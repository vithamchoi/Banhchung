package com.quannhabaninh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardResponse {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer discountPercentage;
    private Integer validityMonths;
    private String benefits;
    private String color;
    private String icon;
    private Boolean isActive;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
