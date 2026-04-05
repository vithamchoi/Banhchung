# Hệ Thống Thông Báo Đơn Hàng Real-Time

## Tổng Quan

Hệ thống WebSocket cho phép admin nhận thông báo real-time về đơn hàng mới, thay đổi trạng thái và hủy đơn. Sử dụng STOMP protocol qua SockJS để đảm bảo tương thích với nhiều trình duyệt.

## Kiến Trúc

```
Client (Admin Dashboard)
    ↓ WebSocket Connection
    ↓ STOMP/SockJS
Backend (Spring Boot)
    ↓ @MessageMapping
OrderNotificationService
    ↓ SimpMessagingTemplate
    ↓ Broadcast to /topic/orders
All Subscribed Clients
```

## Backend Implementation

### 1. Dependencies (pom.xml)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

### 2. WebSocket Configuration

**File:** `WebSocketConfig.java`

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Simple in-memory broker for messages to clients
        config.enableSimpleBroker("/topic", "/queue");
        // Prefix for @MessageMapping methods
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint: ws://localhost:8080/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();  // Fallback for browsers without WebSocket
    }
}
```

**Endpoints:**
- **WebSocket:** `ws://localhost:8080/ws`
- **SockJS:** `http://localhost:8080/ws`

**Topics:**
- `/topic/orders` - Broadcast đơn hàng mới/cập nhật (all admins)
- `/topic/order-stats` - Broadcast thống kê (all admins)
- `/queue/orders` - Riêng cho user (specific user)

### 3. Order Notification DTO

**File:** `OrderNotification.java`

```java
@Data
@Builder
public class OrderNotification {
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private String shippingAddress;
    private LocalDateTime createdAt;
    private Integer itemCount;
    private List<OrderItemSummary> items;
    private String notificationType;  // "NEW_ORDER", "STATUS_CHANGE", "CANCELLED"
    
    @Data
    @Builder
    public static class OrderItemSummary {
        private String productName;
        private Integer quantity;
        private BigDecimal price;
    }
}
```

### 4. Notification Service

**File:** `OrderNotificationService.java`

**Key Methods:**

```java
public void notifyNewOrder(Order order) {
    OrderNotification notification = buildOrderNotification(order, "NEW_ORDER");
    messagingTemplate.convertAndSend("/topic/orders", notification);
}

public void notifyOrderStatusChange(Order order) {
    OrderNotification notification = buildOrderNotification(order, "STATUS_CHANGE");
    
    // Broadcast to all admins
    messagingTemplate.convertAndSend("/topic/orders", notification);
    
    // Send to specific user
    messagingTemplate.convertAndSendToUser(
        order.getUser().getUsername(),
        "/queue/orders",
        notification
    );
}

public void notifyOrderCancelled(Order order) {
    OrderNotification notification = buildOrderNotification(order, "CANCELLED");
    messagingTemplate.convertAndSend("/topic/orders", notification);
}
```

### 5. OrderService Integration

**File:** `OrderService.java`

```java
private final OrderNotificationService orderNotificationService;

@Transactional
public OrderResponse checkout(User user, CheckoutRequest request) {
    // ... create order logic
    
    // Send real-time notification
    orderNotificationService.notifyNewOrder(order);
    
    return mapToOrderResponse(order);
}

@Transactional
public OrderResponse cancelOrder(User user, Long orderId) {
    // ... cancel order logic
    
    // Send cancellation notification
    orderNotificationService.notifyOrderCancelled(order);
    
    return mapToOrderResponse(order);
}
```

### 6. Admin Order Controller

**File:** `AdminOrderController.java`

**REST Endpoints:**

```java
// GET /api/admin/orders - Lấy tất cả đơn hàng
@GetMapping
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> getAllOrders(
    @RequestParam(required = false) OrderStatus status,
    @RequestParam(required = false) String search)

// GET /api/admin/orders/{id} - Lấy 1 đơn hàng
@GetMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> getOrderById(@PathVariable Long id)

// PUT /api/admin/orders/{id}/status - Cập nhật trạng thái
@PutMapping("/{id}/status")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> updateOrderStatus(
    @PathVariable Long id,
    @RequestBody Map<String, String> request)

// GET /api/admin/orders/stats - Thống kê
@GetMapping("/stats")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> getOrderStats()

// GET /api/admin/orders/recent - Đơn hàng gần đây
@GetMapping("/recent")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> getRecentOrders(@RequestParam int limit)
```

**WebSocket Handlers:**

```java
// Subscribe handler - Khi admin kết nối
@SubscribeMapping("/orders")
public List<OrderNotification> subscribeToOrders() {
    // Trả về 5 đơn pending gần nhất
    List<Order> pendingOrders = orderRepository
        .findByStatusOrderByCreatedAtDesc(OrderStatus.PENDING);
    return pendingOrders.stream()
        .limit(5)
        .map(order -> buildNotification(order))
        .collect(Collectors.toList());
}

// Message handler - Refresh stats
@MessageMapping("/orders/refresh-stats")
@SendTo("/topic/order-stats")
public Map<String, Object> refreshStats() {
    Map<String, Object> stats = new HashMap<>();
    stats.put("totalOrders", orderRepository.count());
    stats.put("pendingOrders", orderRepository.countByStatus(PENDING));
    return stats;
}
```

### 7. Repository Updates

**File:** `OrderRepository.java`

**New Methods:**

```java
List<Order> findAllByOrderByCreatedAtDesc();
List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);
List<Order> findByOrderNumberContainingOrShippingNameContainingOrderByCreatedAtDesc(
    String orderNumber, String shippingName);
List<Order> findTop10ByOrderByCreatedAtDesc();
long countByStatus(OrderStatus status);
List<Order> findByCreatedAtAfter(LocalDateTime dateTime);
```

## API Endpoints Summary

### REST APIs

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/admin/orders` | Admin | Lấy tất cả đơn hàng |
| GET | `/api/admin/orders?status=PENDING` | Admin | Lọc theo trạng thái |
| GET | `/api/admin/orders?search=ORD123` | Admin | Tìm kiếm đơn |
| GET | `/api/admin/orders/{id}` | Admin | Chi tiết 1 đơn |
| PUT | `/api/admin/orders/{id}/status` | Admin | Cập nhật trạng thái |
| GET | `/api/admin/orders/stats` | Admin | Thống kê tổng quan |
| GET | `/api/admin/orders/recent` | Admin | Đơn hàng gần đây |

### WebSocket Topics

| Topic | Type | Description | Payload |
|-------|------|-------------|---------|
| `/topic/orders` | Subscribe | Nhận đơn hàng mới/cập nhật | `OrderNotification` |
| `/topic/order-stats` | Subscribe | Nhận thống kê | `Map<String, Object>` |
| `/queue/orders` | User Queue | Thông báo riêng cho user | `OrderNotification` |
| `/app/orders/refresh-stats` | Message | Request refresh stats | - |

### WebSocket Endpoints

| Endpoint | Type | Description |
|----------|------|-------------|
| `/ws` | Connect | WebSocket connection |
| `/ws/info` | SockJS | SockJS info endpoint |
| `/ws/websocket` | SockJS | SockJS WebSocket endpoint |

## Frontend Implementation Guide

### 1. Install Dependencies

```bash
npm install sockjs-client @stomp/stompjs
```

### 2. WebSocket Client Setup

```typescript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

// Create WebSocket client
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = new Client({
  webSocketFactory: () => socket,
  reconnectDelay: 5000,
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000,
});

// Connect handler
stompClient.onConnect = (frame) => {
  console.log('Connected:', frame);
  
  // Subscribe to order notifications
  stompClient.subscribe('/topic/orders', (message) => {
    const notification = JSON.parse(message.body);
    handleNewOrderNotification(notification);
  });
  
  // Subscribe to stats
  stompClient.subscribe('/topic/order-stats', (message) => {
    const stats = JSON.parse(message.body);
    updateStats(stats);
  });
};

// Error handler
stompClient.onStompError = (frame) => {
  console.error('STOMP error:', frame);
};

// Activate connection
stompClient.activate();
```

### 3. Handle Notifications

```typescript
const handleNewOrderNotification = (notification: OrderNotification) => {
  console.log('New order:', notification);
  
  switch (notification.notificationType) {
    case 'NEW_ORDER':
      // Show toast/alert
      toast.success(`Đơn hàng mới: ${notification.orderNumber}`);
      // Add to order list
      setOrders(prev => [notification, ...prev]);
      // Play sound
      playNotificationSound();
      break;
      
    case 'STATUS_CHANGE':
      // Update order in list
      setOrders(prev => prev.map(order => 
        order.orderId === notification.orderId ? notification : order
      ));
      toast.info(`Đơn ${notification.orderNumber} đã ${notification.status}`);
      break;
      
    case 'CANCELLED':
      toast.warning(`Đơn ${notification.orderNumber} đã hủy`);
      // Update or remove from list
      break;
  }
};
```

### 4. Send Messages to Server

```typescript
// Request stats refresh
const refreshStats = () => {
  if (stompClient && stompClient.connected) {
    stompClient.publish({
      destination: '/app/orders/refresh-stats'
    });
  }
};
```

### 5. Cleanup on Unmount

```typescript
useEffect(() => {
  // Connect
  stompClient.activate();
  
  // Cleanup
  return () => {
    if (stompClient) {
      stompClient.deactivate();
    }
  };
}, []);
```

## Notification Types

### 1. NEW_ORDER

**Trigger:** Khi user checkout thành công

**Payload:**
```json
{
  "orderId": 123,
  "orderNumber": "ORD20260204141530001",
  "userId": 2,
  "customerName": "Nguyễn Văn A",
  "customerEmail": "user@example.com",
  "customerPhone": "0987654321",
  "totalAmount": 250000,
  "status": "PENDING",
  "paymentMethod": "COD",
  "shippingAddress": "123 Đường ABC, Hà Nội",
  "createdAt": "2026-02-04T14:15:30",
  "itemCount": 3,
  "items": [
    {
      "productName": "Bánh chưng truyền thống",
      "quantity": 2,
      "price": 100000
    }
  ],
  "notificationType": "NEW_ORDER"
}
```

### 2. STATUS_CHANGE

**Trigger:** Khi admin cập nhật trạng thái đơn

**Payload:** Tương tự NEW_ORDER với `notificationType: "STATUS_CHANGE"`

**Status Flow:**
- PENDING → CONFIRMED
- CONFIRMED → SHIPPING
- SHIPPING → DELIVERED
- Any → CANCELLED

### 3. CANCELLED

**Trigger:** Khi user hoặc admin hủy đơn

**Payload:** Tương tự với `notificationType: "CANCELLED"`

## Testing

### 1. Test WebSocket Connection

```bash
# Sử dụng wscat
npm install -g wscat

# Connect to WebSocket
wscat -c ws://localhost:8080/ws/websocket

# Send STOMP connect frame
CONNECT
accept-version:1.1,1.2
heart-beat:10000,10000

^@

# Subscribe to topic
SUBSCRIBE
id:sub-0
destination:/topic/orders

^@
```

### 2. Test with cURL

```bash
# Tạo đơn hàng mới (trigger notification)
curl -X POST http://localhost:8080/api/orders/checkout \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{...checkout request...}'

# Cập nhật trạng thái (trigger notification)
curl -X PUT http://localhost:8080/api/admin/orders/1/status \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "CONFIRMED"}'

# Lấy stats
curl -H "Authorization: Bearer ADMIN_TOKEN" \
  http://localhost:8080/api/admin/orders/stats
```

### 3. Test Frontend

```typescript
// Log all messages
stompClient.onWebSocketError = (error) => {
  console.error('WebSocket error:', error);
};

stompClient.debug = (str) => {
  console.log('STOMP debug:', str);
};
```

## Performance Considerations

### 1. Connection Management

- **Reconnect:** Auto-reconnect sau 5s nếu mất kết nối
- **Heartbeat:** Ping/pong mỗi 4s để keep-alive
- **Timeout:** 30s timeout cho kết nối ban đầu

### 2. Message Size

- Giới hạn items trong notification: 10 items đầu tiên
- Compress lớn objects nếu cần
- Sử dụng pagination cho danh sách lớn

### 3. Scaling

**Single Server:**
- SimpleBroker (in-memory) - OK cho < 1000 concurrent connections

**Multi-Server:**
- Cần external message broker: RabbitMQ, ActiveMQ
- Update config:
```java
@Override
public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableStompBrokerRelay("/topic", "/queue")
          .setRelayHost("localhost")
          .setRelayPort(61613)
          .setClientLogin("guest")
          .setClientPasscode("guest");
}
```

## Security

### 1. Authentication

**Option 1: JWT in WebSocket Header**
```typescript
const socket = new SockJS('http://localhost:8080/ws', null, {
  transports: ['websocket'],
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

**Option 2: Token in URL (not recommended)**
```typescript
const socket = new SockJS(`http://localhost:8080/ws?token=${token}`);
```

### 2. Authorization

```java
@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    
    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
            .simpDestMatchers("/app/**").authenticated()
            .simpSubscribeDestMatchers("/topic/orders").hasRole("ADMIN")
            .anyMessage().denyAll();
    }
}
```

### 3. CORS

```java
registry.addEndpoint("/ws")
        .setAllowedOriginPatterns("http://localhost:3000", "https://yourdomain.com")
        .withSockJS();
```

## Troubleshooting

### 1. Connection Failed

**Symptoms:** Cannot connect to WebSocket

**Solutions:**
- Check backend is running on port 8080
- Verify CORS settings
- Check firewall/proxy settings
- Try SockJS fallback: `/ws/xhr_streaming`

### 2. No Notifications Received

**Symptoms:** Connected but no messages

**Solutions:**
- Verify subscription: `stompClient.subscribe('/topic/orders', ...)`
- Check admin role permissions
- Test with manual order creation
- Check server logs for errors

### 3. Duplicate Messages

**Symptoms:** Same notification received multiple times

**Solutions:**
- Ensure only one stompClient instance
- Unsubscribe on component unmount
- Check for multiple subscribe calls

### 4. Memory Leak

**Symptoms:** Browser memory increases over time

**Solutions:**
- Always call `stompClient.deactivate()` on unmount
- Clear message arrays periodically
- Limit notification history size

## Best Practices

1. **Always cleanup:** Deactivate WebSocket on component unmount
2. **Handle reconnect:** Show UI indicator when disconnected
3. **Limit history:** Keep only last 50 notifications in memory
4. **Debounce updates:** Don't update UI on every message
5. **Error handling:** Show user-friendly errors
6. **Logging:** Log all WebSocket events in development
7. **Testing:** Test with slow/unstable network
8. **Monitoring:** Track connection metrics (uptime, message count)

## Example Stats Response

```json
{
  "totalOrders": 1250,
  "pendingOrders": 12,
  "confirmedOrders": 45,
  "shippingOrders": 23,
  "deliveredOrders": 1100,
  "cancelledOrders": 70,
  "totalRevenue": 125000000,
  "todayOrderCount": 34,
  "todayRevenue": 3400000
}
```

---

**Version:** 1.0.0  
**Last Updated:** 04/02/2026  
**Technology:** Spring Boot WebSocket, STOMP, SockJS
