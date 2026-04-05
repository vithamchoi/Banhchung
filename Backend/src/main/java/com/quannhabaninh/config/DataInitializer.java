package com.quannhabaninh.config;

import com.quannhabaninh.dto.CreateCardRequest;
import com.quannhabaninh.dto.StorySectionRequest;
import com.quannhabaninh.entity.*;
import com.quannhabaninh.repository.*;
import com.quannhabaninh.service.MembershipCardService;
import com.quannhabaninh.service.StorySectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * DataInitializer - Tự động khởi tạo dữ liệu mẫu khi ứng dụng khởi động.
 * Điều khiển qua property app.data.init:
 *   true  = chỉ insert khi bảng trống
 *   force = xóa sạch toàn bộ rồi insert lại (reset DB)
 *   false = tắt hoàn toàn
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    @Value("${app.data.init:true}")
    private String dataInitMode;

    // ── Repositories ──
    private final UserRepository             userRepository;
    private final MembershipCardRepository   membershipCardRepository;
    private final ProductRepository          productRepository;
    private final StorySectionRepository     storySectionRepository;
    private final CartRepository             cartRepository;
    private final CartItemRepository         cartItemRepository;
    private final OrderRepository            orderRepository;
    private final OrderItemRepository        orderItemRepository;

    // ── Services ──
    private final MembershipCardService membershipCardService;
    private final StorySectionService   storySectionService;

    private final PasswordEncoder passwordEncoder;

    // ────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if ("false".equalsIgnoreCase(dataInitMode)) {
            log.info("[DataInitializer] app.data.init=false – bỏ qua toàn bộ.");
            return;
        }

        boolean forceReset = "force".equalsIgnoreCase(dataInitMode);

        if (forceReset) {
            log.warn("[DataInitializer] *** FORCE RESET – Xóa sạch toàn bộ dữ liệu mẫu ***");
            clearAllData();
        }

        log.info("=== [DataInitializer] Bắt đầu khởi tạo dữ liệu mẫu (mode={}) ===", dataInitMode);

        List<MembershipCard> cards    = initMembershipCards();
        List<User>           users    = initUsers(cards);
        List<Product>        products = initProducts();
        initStorySections();
        initCartsAndItems(users, products);
        initOrdersAndItems(users, products);

        log.info("=== [DataInitializer] Hoàn tất khởi tạo dữ liệu mẫu ===");
    }

    /**
     * Xóa sạch toàn bộ data theo đúng thứ tự khóa ngoại.
     */
    private void clearAllData() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        storySectionRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        membershipCardRepository.deleteAll();
        log.warn("[DataInitializer] Đã xóa sạch toàn bộ dữ liệu.");
    }

    // ════════════════════════════════════════════════════════════════
    // 1. MEMBERSHIP CARDS
    // ════════════════════════════════════════════════════════════════
    private List<MembershipCard> initMembershipCards() {
        if (membershipCardRepository.count() > 0) {
            log.info("[DataInitializer] membership_cards đã có dữ liệu – bỏ qua.");
            return membershipCardRepository.findAllByOrderByDisplayOrderAsc();
        }

        log.info("[DataInitializer] Tạo dữ liệu membership_cards...");

        Object[][] data = {
            {"Thẻ Đồng",      "Thẻ thành viên nhập môn cho khách hàng đăng ký lần đầu",
             20000,  3,  3, "Giảm 3% đơn hàng đầu tiên", "#CD7F32", "bronze-card", 1},
            {"Thẻ Bạc",       "Thẻ thành viên cơ bản dành cho khách hàng mới",
             50000,  5,  6, "Giảm 5% toàn bộ đơn hàng; Ưu tiên tư vấn qua hotline", "#C0C0C0", "silver-card", 2},
            {"Thẻ Vàng",      "Thẻ thành viên dành cho khách hàng thân thiết",
             100000, 10, 12, "Giảm 10% toàn bộ đơn hàng; Miễn phí giao hàng nội thành", "#FFD700", "gold-card", 3},
            {"Thẻ Bạch Kim",  "Thẻ cao cấp dành cho khách hàng VIP",
             200000, 15, 12, "Giảm 15% toàn bộ đơn hàng; Miễn phí giao hàng toàn quốc; Quà tặng đặc biệt", "#E5E4E2", "platinum-card", 4},
            {"Thẻ Kim Cương", "Thẻ đặc biệt dành cho khách hàng cực VIP",
             500000, 20, 24, "Giảm 20% toàn bộ đơn hàng; Ưu tiên đặt hàng; Giao hàng nhanh 24h", "#B9F2FF", "diamond-card", 5},
        };

        for (Object[] row : data) {
            CreateCardRequest req = new CreateCardRequest();
            req.setName((String) row[0]);
            req.setDescription((String) row[1]);
            req.setPrice(new BigDecimal(((Number) row[2]).longValue()));
            req.setDiscountPercentage((Integer) row[3]);
            req.setValidityMonths((Integer) row[4]);
            req.setBenefits((String) row[5]);
            req.setColor((String) row[6]);
            req.setIcon((String) row[7]);
            req.setIsActive(true);
            req.setDisplayOrder((Integer) row[8]);
            membershipCardService.createCard(req);
        }

        log.info("[DataInitializer] Đã tạo {} membership_cards.", data.length);
        return membershipCardRepository.findAllByOrderByDisplayOrderAsc();
    }

    // ════════════════════════════════════════════════════════════════
    // 2. USERS
    // ════════════════════════════════════════════════════════════════
    private List<User> initUsers(List<MembershipCard> cards) {
        if (userRepository.count() > 0) {
            log.info("[DataInitializer] users đã có dữ liệu – bỏ qua.");
            return userRepository.findAll();
        }

        log.info("[DataInitializer] Tạo dữ liệu users...");

        // cards đã sắp xếp theo display_order: Đồng, Bạc, Vàng, Bạch Kim, Kim Cương
        MembershipCard bronze   = !cards.isEmpty() ? cards.get(0) : null;
        MembershipCard silver   = cards.size() > 1  ? cards.get(1) : null;
        MembershipCard gold     = cards.size() > 2  ? cards.get(2) : null;
        MembershipCard platinum = cards.size() > 3  ? cards.get(3) : null;

        Object[][] data = {
            // username, email, fullName, phone, roles, card
            {"admin",        "admin@banhchung.vn",   "Quản Trị Viên", "0901234567",
                Set.of(Role.ROLE_ADMIN, Role.ROLE_USER), null},
            {"nguyen_van_a", "nguyenvana@gmail.com", "Nguyễn Văn A",  "0912345678",
                Set.of(Role.ROLE_USER), silver},
            {"tran_thi_b",   "tranthib@gmail.com",   "Trần Thị B",    "0923456789",
                Set.of(Role.ROLE_USER), gold},
            {"le_van_c",     "levanc@gmail.com",     "Lê Văn C",      "0934567890",
                Set.of(Role.ROLE_USER), platinum},
            {"pham_thi_d",   "phamthid@gmail.com",   "Phạm Thị D",    "0945678901",
                Set.of(Role.ROLE_USER), bronze},
        };

        String encodedPassword = passwordEncoder.encode("password123");

        for (Object[] row : data) {
            @SuppressWarnings("unchecked")
            Set<Role> roles = (Set<Role>) row[4];
            MembershipCard card = (MembershipCard) row[5];

            User user = User.builder()
                    .username((String) row[0])
                    .email((String) row[1])
                    .fullName((String) row[2])
                    .phoneNumber((String) row[3])
                    .password(encodedPassword)
                    .roles(roles)
                    .enabled(true)
                    .membershipCard(card)
                    .build();

            userRepository.save(user);
        }

        log.info("[DataInitializer] Đã tạo {} users.", data.length);
        return userRepository.findAll();
    }

    // ════════════════════════════════════════════════════════════════
    // 3. PRODUCTS  (dùng trực tiếp productRepository vì createProduct nhận entity)
    // ════════════════════════════════════════════════════════════════
    private List<Product> initProducts() {
        if (productRepository.count() > 0) {
            log.info("[DataInitializer] products đã có dữ liệu – bỏ qua.");
            return productRepository.findAll();
        }

        log.info("[DataInitializer] Tạo dữ liệu products...");

        Object[][] data = {
            // name, description, price, category, image, isBestSeller, ingredients, stock
            {"Bánh Chưng Truyền Thống",
             "Bánh chưng vuông truyền thống gói lá dong xanh, nhân thịt mỡ đậu xanh thơm ngon, nấu 12 tiếng.",
             85000, "Bánh Chưng", "/images/banh-chung-truyen-thong.jpg", true,
             "Gạo nếp cái hoa vàng, đậu xanh, thịt ba chỉ, hành tím, tiêu, lá dong, lạt tre", 50},

            {"Bánh Chưng Gấc",
             "Bánh chưng màu đỏ cam rực rỡ từ gấc tươi, mang lại may mắn và hương vị đặc biệt.",
             95000, "Bánh Chưng", "/images/banh-chung-gac.jpg", true,
             "Gạo nếp, quả gấc, đậu xanh, thịt ba chỉ, hành, tiêu, lá dong", 30},

            {"Bánh Tét Miền Nam",
             "Bánh tét hình trụ đặc trưng miền Nam, nhân chuối hoặc đậu xanh thịt mỡ.",
             75000, "Bánh Tét", "/images/banh-tet-mien-nam.jpg", false,
             "Gạo nếp, chuối sứ / đậu xanh, dừa nạo, lá chuối, lạt tre", 40},

            {"Bánh Chưng Chay",
             "Bánh chưng chay không thịt dành cho người ăn chay, nhân đậu xanh và nấm hương.",
             70000, "Bánh Chưng Chay", "/images/banh-chung-chay.jpg", false,
             "Gạo nếp, đậu xanh, nấm hương, mộc nhĩ, hành tây, tiêu, lá dong", 20},

            {"Bánh Chưng Nếp Cẩm",
             "Bánh chưng tím bắt mắt sử dụng gạo nếp cẩm, tốt cho sức khỏe.",
             90000, "Bánh Chưng", "/images/banh-chung-nep-cam.jpg", false,
             "Gạo nếp cẩm, đậu xanh, thịt ba chỉ, hành, tiêu, lá dong", 25},
        };

        for (Object[] row : data) {
            Product product = new Product();
            product.setName((String) row[0]);
            product.setDescription((String) row[1]);
            product.setPrice(new BigDecimal(((Number) row[2]).longValue()));
            product.setCategory((String) row[3]);
            product.setImage((String) row[4]);
            product.setIsBestSeller((Boolean) row[5]);
            product.setIngredients((String) row[6]);
            product.setStockQuantity((Integer) row[7]);
            productRepository.save(product);
        }

        log.info("[DataInitializer] Đã tạo {} products.", data.length);
        return productRepository.findAll();
    }

    // ════════════════════════════════════════════════════════════════
    // 4. STORY SECTIONS
    // ════════════════════════════════════════════════════════════════
    private void initStorySections() {
        if (storySectionRepository.count() > 0) {
            log.info("[DataInitializer] story_sections đã có dữ liệu – bỏ qua.");
            return;
        }

        log.info("[DataInitializer] Tạo dữ liệu story_sections...");

        // Sử dụng ảnh từ Unsplash CDN (miễn phí, không cần tài khoản)
        Object[][] data = {
            // title, content, imageUrl, imageAlt, displayOrder, sectionType, subtitle, highlightedText, secondImageUrl, secondImageAlt
            {"Câu Chuyện Bánh Chưng",
             "Từ hàng trăm năm trước, bánh chưng đã gắn liền với ngày Tết cổ truyền Việt Nam. "
             + "Hình vuông tượng trưng cho Đất, lớp lá dong xanh mướt ôm trọn hương vị quê hương.",
             "https://images.unsplash.com/photo-1621981386329-7a8df6dd6aa4?w=800&q=80",
             "Bánh chưng truyền thống Việt Nam",
             1, "HEADER", "Hương vị ngàn năm", "Bánh chưng – hồn Tết Việt", null, null},

            {"Nguồn Gốc & Truyền Thuyết",
             "Tương truyền hoàng tử Lang Liêu đã dâng bánh chưng và bánh dầy lên vua Hùng, "
             + "biểu trưng cho lòng hiếu thảo và sự trân trọng đất trời.",
             "https://images.unsplash.com/photo-1563245372-f21724e3856d?w=800&q=80",
             "Truyền thuyết bánh chưng Lang Liêu",
             2, "STORY", "Hơn 4000 năm lịch sử", null,
             "https://images.unsplash.com/photo-1569569970363-df7b6160d111?w=400&q=80",
             "Vua Hùng và hoàng tử Lang Liêu"},

            {"Quy Trình Làm Bánh",
             "Mỗi chiếc bánh chưng tại Quán Nhà Bà Ninh được làm hoàn toàn thủ công: chọn gạo nếp cái hoa vàng, "
             + "đậu xanh cà, thịt ba chỉ ướp gia vị rồi gói bằng lá dong tươi và nấu suốt 12 tiếng.",
             "https://images.unsplash.com/photo-1590301157890-4810ed352733?w=800&q=80",
             "Quy trình làm bánh chưng thủ công",
             3, "PROCESS", "Thủ công – Tâm huyết – Truyền thống", "Nấu 12 tiếng liên tục để bánh dẻo ngon",
             "https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=400&q=80",
             "Gói bánh chưng bằng lá dong"},

            {"Nguyên Liệu Tuyển Chọn",
             "Chúng tôi chỉ sử dụng gạo nếp cái hoa vàng Hải Hậu, đậu xanh vàng óng từ Nghệ An, "
             + "thịt ba chỉ tươi mỗi ngày và lá dong xanh mướt từ Ba Vì – tất cả đều được kiểm định kỹ lưỡng.",
             "https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=800&q=80",
             "Nguyên liệu sạch làm bánh chưng",
             4, "STORY", "Sạch từ nguồn – Ngon từ tâm", null, null, null},

            {"Đặt Bánh Ngay Hôm Nay",
             "Hãy để Quán Nhà Bà Ninh mang hương vị Tết cổ truyền đến tận tay bạn và gia đình. "
             + "Giao hàng toàn quốc, đảm bảo bánh tươi ngon khi đến tay khách hàng.",
             "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=1200&q=80",
             "Đặt bánh chưng online giao tận nhà",
             5, "CALL_TO_ACTION", "Giao hàng toàn quốc – Tươi ngon đảm bảo",
             "Đặt ngay – Nhận ưu đãi lên đến 20%", null, null},
        };

        for (Object[] row : data) {
            StorySectionRequest req = StorySectionRequest.builder()
                    .title((String) row[0])
                    .content((String) row[1])
                    .imageUrl((String) row[2])
                    .imageAlt((String) row[3])
                    .displayOrder((Integer) row[4])
                    .isActive(true)
                    .sectionType((String) row[5])
                    .subtitle((String) row[6])
                    .highlightedText((String) row[7])
                    .secondImageUrl((String) row[8])
                    .secondImageAlt((String) row[9])
                    .build();
            storySectionService.createSection(req);
        }

        log.info("[DataInitializer] Đã tạo {} story_sections.", data.length);
    }


    // ════════════════════════════════════════════════════════════════
    // 5. CARTS & CART ITEMS
    // ════════════════════════════════════════════════════════════════
    private void initCartsAndItems(List<User> users, List<Product> products) {
        if (cartRepository.count() > 0) {
            log.info("[DataInitializer] carts đã có dữ liệu – bỏ qua.");
            return;
        }

        log.info("[DataInitializer] Tạo dữ liệu carts & cart_items...");

        if (products.size() < 5) {
            log.warn("[DataInitializer] Không đủ sản phẩm để tạo cart_items.");
            return;
        }

        User userA = findUserByUsername(users, "nguyen_van_a");
        User userB = findUserByUsername(users, "tran_thi_b");
        User userC = findUserByUsername(users, "le_van_c");
        User userD = findUserByUsername(users, "pham_thi_d");

        Product p1 = products.get(0);
        Product p2 = products.get(1);
        Product p3 = products.get(2);
        Product p4 = products.get(3);
        Product p5 = products.get(4);

        if (userA != null) { addItemToCart(userA, p1, 2); addItemToCart(userA, p3, 1); }
        if (userB != null) { addItemToCart(userB, p2, 3); }
        if (userC != null) { addItemToCart(userC, p4, 2); }
        if (userD != null) { addItemToCart(userD, p5, 1); }

        log.info("[DataInitializer] Đã tạo carts & cart_items.");
    }

    /** Tìm hoặc tạo giỏ hàng rồi thêm item */
    private void addItemToCart(User user, Product product, int quantity) {
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart c = new Cart();
            c.setUser(user);
            return cartRepository.save(c);
        });

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setPrice(product.getPrice());
        cartItemRepository.save(item);
    }

    // ════════════════════════════════════════════════════════════════
    // 6. ORDERS & ORDER ITEMS
    // ════════════════════════════════════════════════════════════════
    private void initOrdersAndItems(List<User> users, List<Product> products) {
        if (orderRepository.count() > 0) {
            log.info("[DataInitializer] orders đã có dữ liệu – bỏ qua.");
            return;
        }

        log.info("[DataInitializer] Tạo dữ liệu orders & order_items...");

        if (products.size() < 5) {
            log.warn("[DataInitializer] Không đủ sản phẩm để tạo orders.");
            return;
        }

        User userA = findUserByUsername(users, "nguyen_van_a");
        User userB = findUserByUsername(users, "tran_thi_b");
        User userC = findUserByUsername(users, "le_van_c");
        User userD = findUserByUsername(users, "pham_thi_d");

        Product p1 = products.get(0);
        Product p2 = products.get(1);
        Product p3 = products.get(2);
        Product p4 = products.get(3);
        Product p5 = products.get(4);

        // Đơn 1 – Nguyễn Văn A – DELIVERED – COD
        if (userA != null) {
            List<Object[]> items1 = new ArrayList<>();
            items1.add(new Object[]{p1, 2});
            items1.add(new Object[]{p3, 1});
            createOrder("ORD-20260101-001", userA,
                    OrderStatus.DELIVERED, PaymentMethod.COD, "PAID",
                    "Nguyễn Văn A", "0912345678", "nguyenvana@gmail.com",
                    "123 Đường Láng", "Hà Nội", "Đống Đa", "Láng Hạ", null, items1);
        }

        // Đơn 2 – Trần Thị B – CONFIRMED – BANK_TRANSFER
        if (userB != null) {
            List<Object[]> items2 = new ArrayList<>();
            items2.add(new Object[]{p2, 3});
            createOrder("ORD-20260115-002", userB,
                    OrderStatus.CONFIRMED, PaymentMethod.BANK_TRANSFER, "PAID",
                    "Trần Thị B", "0923456789", "tranthib@gmail.com",
                    "456 Nguyễn Trãi", "Hà Nội", "Thanh Xuân", "Khương Đình",
                    "Giao buổi sáng trước 10h", items2);
        }

        // Đơn 3 – Lê Văn C – PENDING – MOMO
        if (userC != null) {
            List<Object[]> items3 = new ArrayList<>();
            items3.add(new Object[]{p4, 2});
            createOrder("ORD-20260120-003", userC,
                    OrderStatus.PENDING, PaymentMethod.MOMO, "UNPAID",
                    "Lê Văn C", "0934567890", "levanc@gmail.com",
                    "789 Trần Hưng Đạo", "TP.HCM", "Quận 1", "Phạm Ngũ Lão", null, items3);
        }

        // Đơn 4 – Phạm Thị D – PROCESSING – VNPAY
        if (userD != null) {
            List<Object[]> items4 = new ArrayList<>();
            items4.add(new Object[]{p5, 1});
            createOrder("ORD-20260205-004", userD,
                    OrderStatus.PROCESSING, PaymentMethod.VNPAY, "PAID",
                    "Phạm Thị D", "0945678901", "phamthid@gmail.com",
                    "321 Điện Biên Phủ", "TP.HCM", "Bình Thạnh", "Phường 22",
                    "Để ngoài cửa nếu không có nhà", items4);
        }

        // Đơn 5 – Nguyễn Văn A – SHIPPING – COD
        if (userA != null) {
            List<Object[]> items5 = new ArrayList<>();
            items5.add(new Object[]{p1, 2});
            items5.add(new Object[]{p2, 1});
            createOrder("ORD-20260220-005", userA,
                    OrderStatus.SHIPPING, PaymentMethod.COD, "UNPAID",
                    "Nguyễn Văn A", "0912345678", "nguyenvana@gmail.com",
                    "123 Đường Láng", "Hà Nội", "Đống Đa", "Láng Hạ",
                    "Gọi điện trước khi giao", items5);
        }

        log.info("[DataInitializer] Đã tạo orders & order_items.");
    }

    /** Tạo một Order kèm OrderItems từ danh sách {Product, quantity} */
    private void createOrder(String orderNumber, User user,
                             OrderStatus status, PaymentMethod paymentMethod, String paymentStatus,
                             String shippingName, String shippingPhone, String shippingEmail,
                             String shippingAddress, String shippingCity, String shippingDistrict,
                             String shippingWard, String notes,
                             List<Object[]> items) {

        BigDecimal subtotal = BigDecimal.ZERO;
        for (Object[] item : items) {
            Product p = (Product) item[0];
            int qty   = (int) item[1];
            subtotal = subtotal.add(p.getPrice().multiply(BigDecimal.valueOf(qty)));
        }

        BigDecimal shippingFee = (shippingCity.contains("HCM") || shippingCity.contains("Hồ Chí Minh"))
                ? new BigDecimal("30000") : new BigDecimal("20000");

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (user.getMembershipCard() != null) {
            int pct = user.getMembershipCard().getDiscountPercentage() != null
                    ? user.getMembershipCard().getDiscountPercentage() : 0;
            discountAmount = subtotal.multiply(BigDecimal.valueOf(pct))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        BigDecimal totalAmount = subtotal.add(shippingFee).subtract(discountAmount);

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setUser(user);
        order.setStatus(status);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus(paymentStatus);
        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmount);
        order.setShippingName(shippingName);
        order.setShippingPhone(shippingPhone);
        order.setShippingEmail(shippingEmail);
        order.setShippingAddress(shippingAddress);
        order.setShippingCity(shippingCity);
        order.setShippingDistrict(shippingDistrict);
        order.setShippingWard(shippingWard);
        order.setNotes(notes);

        Order savedOrder = orderRepository.save(order);

        for (Object[] item : items) {
            Product p = (Product) item[0];
            int qty   = (int) item[1];

            OrderItem oi = new OrderItem();
            oi.setOrder(savedOrder);
            oi.setProduct(p);
            oi.setProductName(p.getName());
            oi.setProductImage(p.getImage());
            oi.setQuantity(qty);
            oi.setPrice(p.getPrice());
            oi.setSubtotal(p.getPrice().multiply(BigDecimal.valueOf(qty)));
            orderItemRepository.save(oi);
        }
    }

    // ── Helper ──────────────────────────────────────────────────────
    private User findUserByUsername(List<User> users, String username) {
        return users.stream()
                .filter(u -> username.equals(u.getUsername()))
                .findFirst()
                .orElse(null);
    }
}

