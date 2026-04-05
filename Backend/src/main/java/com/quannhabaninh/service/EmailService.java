package com.quannhabaninh.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Mã OTP đặt lại mật khẩu – Tiệm Nhà Ông Sơn");

            String htmlContent = buildOtpEmailContent(otpCode);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau.");
        }
    }

    /**
     * Gửi thông báo đơn hàng mới cho admin.
     */
    @Async
    public void sendNewOrderNotification(String toEmail, String orderNumber,
                                         String customerName, String totalAmount) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🛒 Đơn hàng mới #" + orderNumber + " – Tiệm Nhà Ông Sơn");
            helper.setText(buildNewOrderEmailContent(orderNumber, customerName, totalAmount), true);

            mailSender.send(message);
            logger.info("New order notification email sent to admin: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send new order notification to {}: {}", toEmail, e.getMessage(), e);
            // Không throw – thông báo email lỗi không nên làm hỏng luồng đặt hàng
        }
    }

    private String buildNewOrderEmailContent(String orderNumber, String customerName, String totalAmount) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head><meta charset="UTF-8"></head>
                <body style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px; margin: 0;">
                  <div style="max-width: 560px; margin: 0 auto; background: #ffffff; border-radius: 12px;
                              border: 1px solid #e0e0e0; padding: 40px;">

                    <h2 style="color: #1a5c2a; font-size: 22px; margin-bottom: 8px;">Tiệm Nhà Ông Sơn</h2>
                    <hr style="border: none; border-top: 2px solid #e8f5e9; margin-bottom: 24px;" />

                    <div style="background:#fff8e1; border-left: 4px solid #ffa000; padding: 14px 18px;
                                border-radius: 8px; margin-bottom: 24px;">
                      <p style="margin:0; font-size: 16px; font-weight: bold; color: #5d4037;">
                        🛒 Đơn hàng mới vừa được đặt!
                      </p>
                    </div>

                    <table style="width:100%%; border-collapse: collapse; font-size: 15px; color: #333;">
                      <tr>
                        <td style="padding: 8px 0; font-weight: bold; width: 40%%;">Đơn hàng #:</td>
                        <td style="padding: 8px 0;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding: 8px 0; font-weight: bold;">Khách hàng:</td>
                        <td style="padding: 8px 0;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding: 8px 0; font-weight: bold;">Tổng tiền:</td>
                        <td style="padding: 8px 0; color: #c0392b; font-weight: bold;">%s</td>
                      </tr>
                    </table>

                    <p style="margin-top: 24px; font-size: 14px; color: #555;">
                      Vui lòng đăng nhập vào bảng quản lý để xỮd lý đơn hàng.
                    </p>

                    <hr style="border: none; border-top: 1px solid #e0e0e0; margin-top: 28px;" />
                    <p style="color: #999; font-size: 13px; text-align: center; margin-top: 16px;">
                      Trân trọng, <strong style="color: #1a5c2a;">Tiệm Nhà Ông Sơn</strong>
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(orderNumber, customerName, totalAmount);
    }

    private String buildOtpEmailContent(String otpCode) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head><meta charset="UTF-8"></head>
                <body style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px; margin: 0;">
                  <div style="max-width: 560px; margin: 0 auto; background: #ffffff; border-radius: 12px;
                              border: 1px solid #e0e0e0; padding: 40px;">
                
                    <h2 style="color: #1a5c2a; font-size: 22px; margin-bottom: 8px;">Tiệm Nhà Ông Sơn</h2>
                    <hr style="border: none; border-top: 2px solid #e8f5e9; margin-bottom: 24px;" />
                
                    <p style="color: #333; font-size: 15px; margin-bottom: 12px;">Xin chào,</p>
                
                    <p style="color: #333; font-size: 15px; margin-bottom: 24px;">
                      Bạn vừa yêu cầu đặt lại mật khẩu cho tài khoản tại <strong>Tiệm Nhà Ông Sơn</strong>.
                      <br/>Vui lòng sử dụng mã OTP dưới đây để xác thực:
                    </p>
                
                    <div style="background: #f0faf2; border: 2px dashed #2e7d32; border-radius: 10px;
                                text-align: center; padding: 20px 0; margin-bottom: 24px;">
                      <span style="font-size: 40px; font-weight: bold; letter-spacing: 10px; color: #1a5c2a;">
                        %s
                      </span>
                    </div>
                
                    <p style="color: #e65100; font-size: 14px; margin-bottom: 24px;">
                      ⏳ Mã OTP có hiệu lực trong <strong>10 phút</strong>.
                    </p>
                
                    <div style="background: #fff8e1; border-left: 4px solid #ffa000; padding: 12px 16px;
                                border-radius: 4px; margin-bottom: 24px;">
                      <p style="color: #5d4037; font-size: 14px; margin: 0 0 6px 0; font-weight: bold;">
                        Để bảo vệ tài khoản của bạn:
                      </p>
                      <ul style="color: #5d4037; font-size: 14px; margin: 0; padding-left: 18px;">
                        <li>Không chia sẻ mã OTP với bất kỳ ai</li>
                        <li>Nhập mã OTP ngay trên trang đặt lại mật khẩu</li>
                      </ul>
                    </div>
                
                    <p style="color: #666; font-size: 13px; margin-bottom: 0;">
                      Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email hoặc liên hệ với chúng tôi.
                    </p>
                
                    <hr style="border: none; border-top: 1px solid #e0e0e0; margin-top: 28px;" />
                    <p style="color: #999; font-size: 13px; text-align: center; margin-top: 16px;">
                      Trân trọng, <strong style="color: #1a5c2a;">Tiệm Nhà Ông Sơn</strong>
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(otpCode);
    }
}
