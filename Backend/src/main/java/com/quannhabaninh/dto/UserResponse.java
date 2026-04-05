package com.quannhabaninh.dto;

import com.quannhabaninh.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private Set<Role> roles;
    private Boolean enabled;
    private CardResponse membershipCard;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
