# Tích Hợp Thẻ Thành Viên với Khách Hàng

## Tổng Quan

Tính năng này cho phép admin gán thẻ thành viên cho khách hàng, hiển thị thông tin thẻ trong danh sách khách hàng, và quản lý mối quan hệ giữa user và membership card.

## Thay Đổi Backend

### 1. Database Schema

**Bảng `users` - Thêm cột mới:**
```sql
ALTER TABLE users
ADD COLUMN membership_card_id BIGINT DEFAULT NULL;

ALTER TABLE users
ADD CONSTRAINT fk_users_membership_card
FOREIGN KEY (membership_card_id) REFERENCES membership_cards(id)
ON DELETE SET NULL;
```

**Relationship:**
- **Many-to-One**: Nhiều user có thể có cùng 1 loại thẻ
- **Optional**: User có thể không có thẻ (NULL)
- **ON DELETE SET NULL**: Khi xóa thẻ, user vẫn tồn tại nhưng `membership_card_id` = NULL

### 2. Entity Updates

**User.java** - Thêm relationship:
```java
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "membership_card_id")
private MembershipCard membershipCard;
```

### 3. New DTOs

**UserResponse.java** - Response với thông tin thẻ:
```java
@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private Set<Role> roles;
    private Boolean enabled;
    private CardResponse membershipCard;  // ← NEW
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**AssignCardRequest.java** - Request gán thẻ:
```java
@Data
public class AssignCardRequest {
    private Long membershipCardId;  // NULL để bỏ gán thẻ
}
```

### 4. Service Updates

**AuthService.java** - Thêm methods:

```java
// Lấy tất cả user kèm thông tin thẻ
public List<UserResponse> getAllUsers() {
    return userRepository.findAll().stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList());
}

// Gán thẻ cho user
@Transactional
public UserResponse assignCardToUser(Long userId, Long cardId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    
    if (cardId == null) {
        user.setMembershipCard(null);  // Bỏ gán thẻ
    } else {
        MembershipCard card = membershipCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.getIsActive()) {
            throw new RuntimeException("Cannot assign inactive card");
        }
        
        user.setMembershipCard(card);
    }
    
    return mapToUserResponse(userRepository.save(user));
}

// Map User → UserResponse
private UserResponse mapToUserResponse(User user) {
    CardResponse cardResponse = null;
    if (user.getMembershipCard() != null) {
        MembershipCard card = user.getMembershipCard();
        cardResponse = CardResponse.builder()
                .id(card.getId())
                .name(card.getName())
                .price(card.getPrice())
                .discountPercentage(card.getDiscountPercentage())
                // ... other fields
                .build();
    }
    
    return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .membershipCard(cardResponse)  // ← Thông tin thẻ
            // ... other fields
            .build();
}
```

### 5. Controller Updates

**AuthController.java** - New endpoints:

```java
// GET /api/auth/admin/users - Trả về UserResponse[] thay vì User[]
@GetMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> getAllUsers() {
    List<UserResponse> users = authService.getAllUsers();
    return ResponseEntity.ok(users);
}

// GET /api/auth/admin/users/{id} - Lấy 1 user kèm thẻ
@GetMapping("/admin/users/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> getUserById(@PathVariable Long id) {
    UserResponse user = authService.getUserById(id);
    return ResponseEntity.ok(user);
}

// PUT /api/auth/admin/users/{userId}/assign-card - Gán thẻ
@PutMapping("/admin/users/{userId}/assign-card")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> assignCardToUser(
        @PathVariable Long userId,
        @RequestBody AssignCardRequest request) {
    UserResponse user = authService.assignCardToUser(userId, request.getMembershipCardId());
    return ResponseEntity.ok(user);
}
```

## Thay Đổi Frontend

### 1. CustomerManagementPage.tsx Updates

**Interfaces:**
```typescript
interface MembershipCard {
  id: number;
  name: string;
  description: string;
  price: number;
  discountPercentage: number;
  validityMonths: number;
  benefits: string;
  color: string;
  icon: string;
  isActive: boolean;
  displayOrder: number;
  createdAt: string;
  updatedAt: string;
}

interface Customer {
  id: number;
  username: string;
  email: string;
  fullName: string;
  phoneNumber: string;
  roles: string[];
  enabled: boolean;
  membershipCard: MembershipCard | null;  // ← NEW
  createdAt: string;
  updatedAt: string;
}
```

**New State:**
```typescript
const [availableCards, setAvailableCards] = useState<MembershipCard[]>([]);
const [assignCardDialogOpen, setAssignCardDialogOpen] = useState(false);
const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);
const [selectedCardId, setSelectedCardId] = useState<number | null>(null);
```

**Fetch Available Cards:**
```typescript
useEffect(() => {
  fetchCustomers();
  fetchAvailableCards();  // ← Load danh sách thẻ
}, []);

const fetchAvailableCards = async () => {
  const token = localStorage.getItem('token');
  const response = await fetch('http://localhost:8080/api/cards/active', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  const data = await response.json();
  setAvailableCards(data);
};
```

**Assign Card Function:**
```typescript
const handleAssignCard = async () => {
  const token = localStorage.getItem('token');
  const response = await fetch(
    `http://localhost:8080/api/auth/admin/users/${selectedCustomer.id}/assign-card`,
    {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ membershipCardId: selectedCardId })
    }
  );
  
  if (response.ok) {
    await fetchCustomers();  // Refresh danh sách
    setAssignCardDialogOpen(false);
  }
};
```

### 2. Table Column - Membership Card

**Thêm cột trong table:**
```tsx
<th>Thẻ thành viên</th>

{/* ... */}

<td className="px-6 py-4">
  {customer.membershipCard ? (
    <div 
      className="inline-flex items-center gap-2 px-3 py-1.5 rounded-lg"
      style={{ 
        backgroundColor: `${customer.membershipCard.color}20`,
        color: customer.membershipCard.color,
        border: `1px solid ${customer.membershipCard.color}40`
      }}
    >
      <CreditCard className="w-4 h-4" />
      <span>{customer.membershipCard.name}</span>
      {customer.membershipCard.discountPercentage && (
        <span className="px-1.5 py-0.5 bg-white/50 rounded text-xs">
          -{customer.membershipCard.discountPercentage}%
        </span>
      )}
    </div>
  ) : (
    <span className="text-gray-400 text-sm italic">Chưa có thẻ</span>
  )}
</td>
```

**Features:**
- Hiển thị badge với màu của thẻ
- Show discount percentage nếu có
- Fallback "Chưa có thẻ" nếu NULL

### 3. Assign Card Dialog

**Dialog Component:**
```tsx
<Dialog open={assignCardDialogOpen} onOpenChange={setAssignCardDialogOpen}>
  <DialogContent>
    <DialogHeader>
      <DialogTitle>Gán Thẻ Thành Viên</DialogTitle>
      <DialogDescription>
        Chọn loại thẻ cho: {selectedCustomer?.fullName}
      </DialogDescription>
    </DialogHeader>

    <div className="space-y-4">
      <select
        value={selectedCardId || ''}
        onChange={(e) => setSelectedCardId(e.target.value ? Number(e.target.value) : null)}
        className="w-full px-3 py-2 border rounded-lg"
      >
        <option value="">Không có thẻ</option>
        {availableCards.map((card) => (
          <option key={card.id} value={card.id}>
            {card.name} - {formatCurrency(card.price)}
            {card.discountPercentage && ` (Giảm ${card.discountPercentage}%)`}
          </option>
        ))}
      </select>

      {/* Preview card info */}
      {selectedCardId && (
        <div className="p-4 bg-gray-50 rounded-lg">
          {/* Show card details, benefits, etc. */}
        </div>
      )}
    </div>

    <DialogFooter>
      <Button variant="outline" onClick={() => setAssignCardDialogOpen(false)}>
        Hủy
      </Button>
      <Button onClick={handleAssignCard}>
        Xác nhận
      </Button>
    </DialogFooter>
  </DialogContent>
</Dialog>
```

**Features:**
- Select dropdown với tất cả thẻ active
- Option "Không có thẻ" để bỏ gán
- Preview thông tin thẻ được chọn
- Confirm button để thực hiện gán

### 4. Action Button

**Thêm cột "Thao tác":**
```tsx
<th>Thao tác</th>

{/* ... */}

<td className="px-6 py-4">
  <button
    onClick={() => handleOpenAssignCardDialog(customer)}
    className="inline-flex items-center gap-1 px-3 py-1.5 text-sm font-medium text-blue-600 hover:bg-blue-50 rounded-lg"
  >
    <Edit2 className="w-4 h-4" />
    Gán thẻ
  </button>
</td>
```

## API Endpoints Summary

### 1. GET /api/auth/admin/users
**Authorization:** Admin only  
**Response:**
```json
[
  {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "fullName": "Administrator",
    "phoneNumber": "0123456789",
    "roles": ["ROLE_ADMIN"],
    "enabled": true,
    "membershipCard": null,
    "createdAt": "2026-02-04T10:00:00",
    "updatedAt": "2026-02-04T10:00:00"
  },
  {
    "id": 2,
    "username": "user1",
    "email": "user1@example.com",
    "fullName": "Nguyễn Văn A",
    "phoneNumber": "0987654321",
    "roles": ["ROLE_USER"],
    "enabled": true,
    "membershipCard": {
      "id": 1,
      "name": "Thẻ Bạc",
      "description": "Thẻ thành viên bạc",
      "price": 100000,
      "discountPercentage": 5,
      "validityMonths": 12,
      "benefits": "- Giảm 5%\n- Ưu tiên đặt hàng",
      "color": "#3b82f6",
      "icon": "CreditCard",
      "isActive": true,
      "displayOrder": 1,
      "createdAt": "2026-02-04T09:00:00",
      "updatedAt": "2026-02-04T09:00:00"
    },
    "createdAt": "2026-02-04T11:00:00",
    "updatedAt": "2026-02-04T12:00:00"
  }
]
```

### 2. GET /api/auth/admin/users/{id}
**Authorization:** Admin only  
**Response:** Single UserResponse object

### 3. PUT /api/auth/admin/users/{userId}/assign-card
**Authorization:** Admin only  
**Request:**
```json
{
  "membershipCardId": 1  // hoặc null để bỏ gán
}
```

**Response:**
```json
{
  "id": 2,
  "username": "user1",
  "membershipCard": {
    "id": 1,
    "name": "Thẻ Bạc",
    ...
  },
  ...
}
```

**Error Cases:**
- 404: User not found
- 404: Card not found
- 400: Cannot assign inactive card

## Hướng Dẫn Sử Dụng

### 1. Migrate Database

```bash
# Chạy migration script
mysql -u root -p banhchung < Backend/src/main/resources/db/migration/add_membership_card_to_users.sql
```

### 2. Test Backend API

```bash
# Lấy danh sách user kèm thẻ
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/auth/admin/users

# Gán thẻ ID=1 cho user ID=2
curl -X PUT \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"membershipCardId": 1}' \
  http://localhost:8080/api/auth/admin/users/2/assign-card

# Bỏ gán thẻ
curl -X PUT \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"membershipCardId": null}' \
  http://localhost:8080/api/auth/admin/users/2/assign-card
```

### 3. Sử Dụng Trên Frontend

1. **Xem thông tin thẻ của khách hàng:**
   - Vào Admin Dashboard → Khách hàng
   - Cột "Thẻ thành viên" hiển thị thẻ hiện tại
   - Badge màu sắc theo loại thẻ
   - Hiển thị % giảm giá nếu có

2. **Gán thẻ cho khách hàng:**
   - Click button "Gán thẻ" ở cột "Thao tác"
   - Dialog mở ra với dropdown chọn thẻ
   - Chọn thẻ từ danh sách (chỉ thẻ active)
   - Hoặc chọn "Không có thẻ" để bỏ gán
   - Click "Xác nhận"

3. **Preview thông tin thẻ:**
   - Khi chọn thẻ trong dialog
   - Phần preview hiển thị:
     - Giá thẻ
     - % giảm giá
     - Thời hạn
     - Quyền lợi chi tiết

## Business Logic

### Validation Rules

1. **Gán thẻ:**
   - Chỉ Admin mới gán được thẻ
   - Chỉ gán được thẻ đang active (isActive=true)
   - Có thể gán NULL để bỏ thẻ
   - Nhiều user có thể cùng 1 loại thẻ

2. **Xóa thẻ:**
   - Khi xóa membership card
   - Tất cả user có thẻ đó → membership_card_id = NULL
   - User vẫn tồn tại (ON DELETE SET NULL)

3. **Hiển thị:**
   - Table hiển thị thẻ với màu sắc riêng
   - Badge động theo color của thẻ
   - Fallback "Chưa có thẻ" cho NULL

### Performance Considerations

1. **EAGER Fetching:**
   - User.membershipCard dùng EAGER
   - Load thẻ cùng lúc với user
   - Tránh N+1 query problem

2. **Index:**
   - Index trên users.membership_card_id
   - Query nhanh khi filter theo thẻ

3. **Caching:**
   - Available cards được cache ở client
   - Chỉ fetch 1 lần khi mount component

## Testing

### Test Cases

1. **Gán thẻ thành công:**
   - User chưa có thẻ → Gán thẻ Bạc
   - Verify: API trả về user với membershipCard
   - Verify: UI hiển thị badge thẻ Bạc

2. **Bỏ gán thẻ:**
   - User đang có thẻ Bạc → Chọn "Không có thẻ"
   - Verify: membershipCard = null
   - Verify: UI hiển thị "Chưa có thẻ"

3. **Đổi thẻ:**
   - User có thẻ Bạc → Gán thẻ Vàng
   - Verify: membershipCard.id = Vàng.id
   - Verify: Badge đổi màu

4. **Error handling:**
   - Gán thẻ inactive → Error "Cannot assign inactive card"
   - Gán thẻ không tồn tại → 404
   - User không tồn tại → 404

5. **Permission:**
   - User thường không thể gán thẻ → 403
   - Chỉ Admin mới thấy button "Gán thẻ"

## Future Enhancements

- [ ] Lịch sử thay đổi thẻ (card_history table)
- [ ] Ngày hết hạn thẻ (expiry_date column)
- [ ] Auto-apply discount khi checkout
- [ ] Notification khi sắp hết hạn thẻ
- [ ] Filter khách hàng theo loại thẻ
- [ ] Thống kê số lượng user theo thẻ
- [ ] Bulk assign card cho nhiều user
- [ ] Export danh sách user theo thẻ

---

**Version:** 1.0.0  
**Last Updated:** 04/02/2026  
**Related:** CARD_MANAGEMENT.md, CUSTOMER_MANAGEMENT.md
