package com.quannhabaninh.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String email;
    private String phoneNumber;
}
