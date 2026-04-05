package com.quannhabaninh.service;

import com.quannhabaninh.dto.AuthResponse;
import com.quannhabaninh.dto.CardResponse;
import com.quannhabaninh.dto.GoogleLoginRequest;
import com.quannhabaninh.dto.LoginRequest;
import com.quannhabaninh.dto.RegisterRequest;
import com.quannhabaninh.dto.UserResponse;
import com.quannhabaninh.entity.MembershipCard;
import com.quannhabaninh.entity.RefreshToken;
import com.quannhabaninh.entity.Role;
import com.quannhabaninh.entity.User;
import com.quannhabaninh.repository.MembershipCardRepository;
import com.quannhabaninh.repository.UserRepository;
import com.quannhabaninh.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final MembershipCardRepository membershipCardRepository;
    private final EmailService emailService;

    // OTP storage: email -> {otp, expiryTime}
    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();

    @Value("${app.otp.expiration-minutes:10}")
    private int otpExpirationMinutes;

    private static final SecureRandom RANDOM = new SecureRandom();

    private record OtpData(String otp, LocalDateTime expiryTime) {}


    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .roles(Collections.singleton(Role.ROLE_USER))
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        // Assign role based on user ID
        assignRoleBasedOnId(savedUser);

        // Refresh user to get updated roles from database
        savedUser = userRepository.findById(savedUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found after creation"));

        // Generate tokens
        String token = tokenProvider.generateTokenFromUsername(savedUser.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser.getId());

        return new AuthResponse(
                token,
                refreshToken.getToken(),
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRoles());
    }

    /**
     * Assign role based on user ID or Admin Email
     * ID 1 = ADMIN
     * ID 2 = USER
     * tiomnhaongsonpdp@gmail.com = ADMIN
     * Others = USER (default)
     */
    private void assignRoleBasedOnId(User user) {
        if (user.getId() == 1 || "tiemnhaongsonpdp@gmail.com".equals(user.getEmail())) {
            user.getRoles().clear();
            user.getRoles().add(Role.ROLE_ADMIN);
            userRepository.save(user);
        } else if (user.getId() == 2) {
            user.getRoles().clear();
            user.getRoles().add(Role.ROLE_USER);
            userRepository.save(user);
        }
        // For other IDs, keep default ROLE_USER
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmailOrPhone(),
                        request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        // Find user by email or phone number
        User user;
        if (request.getEmailOrPhone().contains("@")) {
            user = userRepository.findByEmail(request.getEmailOrPhone())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } else {
            user = userRepository.findByPhoneNumber(request.getEmailOrPhone())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthResponse(
                token,
                refreshToken.getToken(),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRoles());
    }

    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        // Find user by Google ID
        Optional<User> existingUserByGoogleId = userRepository.findByGoogleId(request.getGoogleId());
        
        User user;
        if (existingUserByGoogleId.isPresent()) {
            // User exists, just update profile picture if changed
            user = existingUserByGoogleId.get();
            if (request.getPicture() != null && !request.getPicture().equals(user.getProfilePicture())) {
                user.setProfilePicture(request.getPicture());
                user = userRepository.save(user);
            }
        } else {
            // Check if user exists by email
            Optional<User> existingUserByEmail = userRepository.findByEmail(request.getEmail());
            
            if (existingUserByEmail.isPresent()) {
                // Link Google account to existing user
                user = existingUserByEmail.get();
                user.setGoogleId(request.getGoogleId());
                user.setProfilePicture(request.getPicture());
                user = userRepository.save(user);
            } else {
                // Completely new user
                String username = generateUsernameFromEmail(request.getEmail());
                
                user = User.builder()
                        .username(username)
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                        .fullName(request.getFullName())
                        .googleId(request.getGoogleId())
                        .profilePicture(request.getPicture())
                        .roles(Collections.singleton(Role.ROLE_USER))
                        .enabled(true)
                        .build();
                
                user = userRepository.save(user);
            }
        }

        // Must assign role to ensure hardcoded admin gets ADMIN role
        assignRoleBasedOnId(user);

        // Fetch user from DB again to refresh lazy-loaded collections like Roles
        user = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found after processing Google Login"));

        // Generate tokens
        String token = tokenProvider.generateTokenFromUsername(user.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthResponse(
                token,
                refreshToken.getToken(),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRoles());
    }

    private String generateUsernameFromEmail(String email) {
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int counter = 1;
        
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Update user role (Admin only)
     */
    @Transactional
    public User updateUserRole(Long userId, java.util.Set<Role> roles) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        user.getRoles().clear();
        user.getRoles().addAll(roles);

        return userRepository.save(user);
    }

    /**
     * Get all users (Admin only)
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Assign membership card to user (Admin only)
     */
    @Transactional
    public UserResponse assignCardToUser(Long userId, Long cardId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (cardId == null) {
            user.setMembershipCard(null);
        } else {
            MembershipCard card = membershipCardRepository.findById(cardId)
                    .orElseThrow(() -> new RuntimeException("Membership card not found with ID: " + cardId));

            if (!card.getIsActive()) {
                throw new RuntimeException("Cannot assign inactive membership card");
            }

            user.setMembershipCard(card);
        }

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    /**
     * Get user by ID with membership card info (Admin only)
     */
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return mapToUserResponse(user);
    }

    /**
     * Toggle user enabled status (ban/unban) – Admin only
     */
    @Transactional
    public UserResponse toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        boolean currentStatus = Boolean.TRUE.equals(user.getEnabled());
        user.setEnabled(!currentStatus);
        User saved = userRepository.save(user);
        return mapToUserResponse(saved);
    }


    private UserResponse mapToUserResponse(User user) {
        CardResponse cardResponse = null;
        if (user.getMembershipCard() != null) {
            MembershipCard card = user.getMembershipCard();
            cardResponse = CardResponse.builder()
                    .id(card.getId())
                    .name(card.getName())
                    .description(card.getDescription())
                    .price(card.getPrice())
                    .discountPercentage(card.getDiscountPercentage())
                    .validityMonths(card.getValidityMonths())
                    .benefits(card.getBenefits())
                    .color(card.getColor())
                    .icon(card.getIcon())
                    .isActive(card.getIsActive())
                    .displayOrder(card.getDisplayOrder())
                    .createdAt(card.getCreatedAt())
                    .updatedAt(card.getUpdatedAt())
                    .build();
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .roles(user.getRoles())
                .enabled(user.getEnabled())
                .membershipCard(cardResponse)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    // ===================== OTP / FORGOT PASSWORD =====================

    /**
     * Tạo OTP 6 số, lưu vào store (ghi đè mã cũ nếu có), gửi email.
     */
    public void sendForgotPasswordOtp(String email) {
        // Kiểm tra email tồn tại
        userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống."));

        // Tạo mã OTP 6 số
        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(otpExpirationMinutes);

        // Lưu vào store (ghi đè mã cũ → mã cũ vô hiệu)
        otpStore.put(email, new OtpData(otp, expiryTime));
        logger.info("OTP generated for email: {} (expires: {})", email, expiryTime);

        // Gửi email
        emailService.sendOtpEmail(email, otp);
    }

    /**
     * Xác thực OTP: đúng mã + còn hiệu lực.
     * Trả về true nếu hợp lệ (KHÔNG xóa OTP để cho phép step tiếp theo dùng).
     */
    public boolean verifyOtp(String email, String otp) {
        // Dọn dẹp OTP hết hạn
        otpStore.entrySet().removeIf(e -> e.getValue().expiryTime().isBefore(LocalDateTime.now()));

        OtpData data = otpStore.get(email);
        if (data == null) {
            throw new RuntimeException("Mã OTP không tồn tại hoặc đã hết hạn. Vui lòng gửi lại.");
        }
        if (data.expiryTime().isBefore(LocalDateTime.now())) {
            otpStore.remove(email);
            throw new RuntimeException("Mã OTP đã hết hạn. Vui lòng gửi lại.");
        }
        if (!data.otp().equals(otp.trim())) {
            throw new RuntimeException("Mã OTP không chính xác.");
        }
        return true;
    }

    /**
     * Đặt lại mật khẩu sau khi xác thực OTP thành công.
     */
    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        // Xác thực OTP trước
        verifyOtp(email, otp);

        // Đổi mật khẩu
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống."));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Xóa OTP sau khi dùng thành công
        otpStore.remove(email);
        logger.info("Password reset successfully for email: {}", email);
    }
}
