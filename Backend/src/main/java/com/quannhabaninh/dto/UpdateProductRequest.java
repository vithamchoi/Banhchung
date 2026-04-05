package com.quannhabaninh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Boolean isBestSeller;
    private String ingredients;
    private Integer stockQuantity;
    private Integer weight; // grams
}
