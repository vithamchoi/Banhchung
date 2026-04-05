package com.quannhabaninh.dto;

import com.quannhabaninh.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    
    private String token;
    private String refreshToken;
    
    @Builder.Default
    private String type = "Bearer";
    
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private List<String> roles;
    
    // Constructor without refreshToken (backward compatibility)
    public AuthResponse(String token, Long id, String username, String email, String fullName, Set<Role> roles) {
        this.token = token;
        this.type = "Bearer";
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles.stream().map(Enum::name).collect(Collectors.toList());
    }
    
    // Constructor with refreshToken
    public AuthResponse(String token, String refreshToken, Long id, String username, String email, String fullName, Set<Role> roles) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.type = "Bearer";
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles.stream().map(Enum::name).collect(Collectors.toList());
    }
}
