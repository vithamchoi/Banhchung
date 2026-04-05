package com.quannhabaninh.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginRequest {

    @NotBlank(message = "Google ID is required")
    private String googleId;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    // Removed @NotBlank because sometimes Google User info doesn't have full name properly set or synced
    private String fullName;

    private String picture;
}
