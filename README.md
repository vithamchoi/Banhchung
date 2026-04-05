# Dự án Bánh Chưng (Tiệm Nhà Ông Sơn)
Dự án web bán bánh chưng – fullstack chuyên nghiệp (Frontend React/Vite + Backend Spring Boot).

Hệ thống được thiết kế theo mô hình **E-Commerce (Thương mại điện tử)** chuyên biệt, kết hợp hệ thống **CRM thu nhỏ (Chăm sóc Khách hàng & Thẻ thành viên)**. Toàn bộ thiết kế chia ra 4 luồng (flows) tương tác chính:

## 1. Luồng Người dùng Vãng lai (Guest Flow)
*Dành cho người dùng mới truy cập, chưa có tài khoản.*

*   **Landing Page & Trải nghiệm thị giác:**
    *   Trang chủ áp dụng kỹ thuật cuộn mượt (Scroll Animation) kết hợp 98 frames khung hình tạo thành hoạt hình gói bánh chưng vô cùng bắt mắt.
    *   Trang "Câu chuyện" kể về nguồn gốc hệ thống bán hàng.
*   **Tham khảo Sản phẩm (Product Discovery):**
    *   Danh mục sản phẩm tự động cập nhật hiển thị (Bánh chưng, set quà tặng,...).
    *   Hiển thị đánh giá chung, giá xuất tiền, hình ảnh nổi bật.
*   **Mua sắm nháp (Giỏ hàng bộ nhớ tạm):**
    *   Người dùng thỏa sức thêm sản phẩm, tăng/giảm số lượng vào giỏ hàng ngay lập tức với tốc độ phản hồi tính bằng ms.
    *   Dữ liệu giỏ hàng được găm trong `localStorage` (Bộ nhớ trình duyệt). Thoát trình duyệt không bị mất giỏ.
    *   **Chốt chặn Auth:** Khi khách hàng tiến hành bấm Thanh toán (Checkout), quy trình tạm dừng và yêu cầu Đăng nhập/Đăng ký để xác minh định dạng danh tính người nhận hàng.

## 2. Luồng Xác thực & Bảo mật (Auth Flow)
*Hệ thống quản lý định danh người dùng sử dụng API.*

*   **Đăng ký / Đăng nhập truyền thống:** Sử dụng Email và Mật khẩu cơ bản.
*   **Đăng nhập siêu tốc (SSO):** Tích hợp Đăng nhập bằng **Tài khoản Google** (Google OAuth2).
*   **Tính năng Merge giỏ hàng thông minh:** Ngay sau vòng đăng nhập thành công, Giỏ hàng Guest mà họ vừa lên đồ bên ngoài sẽ được tự động **gộp (merge)** trực tiếp vào Giỏ hàng Backend của chính Tài khoản đó. Khách hàng không bao giờ phải thực hiện chọn lại mặt hàng.
*   **Hệ thống Phân quyền (RBAC):** Tài khoản được gắn Token JWT chuyên biệt. Tự động chia làn giao diện riêng tư thành `Khách hàng (USER)` hay `Quản trị viên (ADMIN)`.

## 3. Luồng Khách hàng Thành viên (Customer Flow)
*Sau khi đăng nhập, hệ sinh thái CRM (Membership cá nhân) sẽ được hệ thống bao phủ phục vụ.*

*   **Quản lý Đơn hàng (Orders):**
    *   Checkout tiến hành xác minh giỏ hàng thật.
    *   Giao diện theo dõi trạng thái tiến độ đơn (Đang xử lý -> Đang giao -> Hoàn tất/Hủy). Toàn bộ lịch sử mua sắm lưu trữ trọn đời.
*   **Hệ sinh thái Thẻ Thành Viên tự động (Loyalty Membership):**
    *   **Automation:** Website hoàn toàn tự động cộng dồn số tiền khách đã chi tiêu từ toàn bộ lượng đơn hàng thành công trước đó.
    *   Tự động xếp hạng và ấn định Thẻ (Bronze, Silver, Gold, Diamond...) tương ứng vào ví điện tử Profile.
    *   **Tính năng đặc quyền Checkout:** Khách sở hữu thẻ (VD: Thẻ Vàng giảm 15%) thì ở mọi đơn hàng sắp đặt, hóa đơn tự động tính toán trừ luôn 15% mà không đòi hỏi bất cứ thao tác nhập mã nào.
*   **Tính năng Tương tác trực tiếp (Live Chat widget):**
    *   Khay Chat ở góc màn hình kết nối trực tiếp khách với tổng đài nhân sự trực Page.

## 4. Luồng Quản trị viên (Admin Flow)
*Bảng điều khiển sau rèm (Behind the Scenes). Bắt buộc phải là ROLE_ADMIN mới có quyền gọi API tải trang.*

*   **Dashboard Thống kê:** Theo dõi biểu đồ tổng quan Doanh Thu, số lượng Users và đơn hàng mua theo biểu đồ thống kê chuyên môn.
*   **Quản lý Catalog & Đơn Hàng:**
    *   Đăng tải bổ sung mặt hàng mới. Cập nhật sửa mô tả/giá bán.
    *   Duyệt qua danh sách đơn hàng thực của hệ thống, cập nhật chuyển đổi quy trình điều phối đơn hàng.
*   **CRM Cấu hình thẻ tự động:**
    *   Giao diện danh mục toàn thể người dùng Website (Users).
    *   **Bộ não Rules Cấp thẻ:** Admin sẽ đóng vai người khởi tạo quy tắc. VD setup: "Chi trên 5 triệu -> Tự động hóa thẻ Gold -> Tự động Sale hệ thống 15% lần sau". Backend quét quy chuẩn này và gán cho tất cả khách hàng ngay tắp lự.
*   **Giao diện Message Ưu tiên thông minh (Priority-based Sorting Inbox):**
    *   Webchat inbox trực trả lời trực tiếp từ hệ thống nội bộ.
    *   **Cơ chế Priority Ranking:** Nền tảng sẽ scan hạng thẻ gốc của mỗi người inbox, sau đó đẩy khách hàng VIP (Các thẻ Diamond, Platinum) lên Top vị trí trên đầu để rep ưu tiên ngay lập tức.
*   **Trình CMS Bài Viết:**
    *   Edit và chèn cấu hình dữ liệu vào giao diện trang "Câu chuyện" bằng Tool trực quan thay vì yêu cầu IT sửa code.

---
### 🛠 Technical Overview brief (Chi tiết Kỹ thuật)
- **Frontend Block:** Sử dụng thuần `React (Vite)`. Cắt giảm phần đông UI component dư thừa từ thư viện lớn và sử dụng Native CSS/Tailwind nhẹ làm chủ đạo. Request qua API REST bằng `axios` auth JWT qua headers. Trạng thái global chạy bằng React Context & custom Hooks.
- **Backend Block:** Phân luồng RESTful controller bằng `Spring Boot (Java)`, xử lí cơ sở dữ liệu Relation RDBMS (MySQL) với mô hình entity rõ rệt. Phân quyền Request Filter theo SecurityFilterChain (`JWT`).
- **Deploy Pipeline:** Frontend kết nối 100% về biến môi trường `VITE_API_URL` Online hoàn toàn độc lập, dễ dàng thay thế, nâng cấp hạ tầng backend riêng rẽ mà chặn hoàn toàn ảnh hưởng gián đoạn phía Client.
