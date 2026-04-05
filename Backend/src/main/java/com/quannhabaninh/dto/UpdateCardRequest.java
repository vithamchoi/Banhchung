package com.quannhabaninh.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCardRequest {
    
    @NotBlank(message = "Tên thẻ không được để trống")
    private String name;
    
    private String description;
    
    @NotNull(message = "Giá thẻ không được để trống")
    @Min(value = 0, message = "Giá thẻ phải lớn hơn hoặc bằng 0")
    private BigDecimal price;
    
    @Min(value = 0, message = "Phần trăm giảm giá phải từ 0-100")
    private Integer discountPercentage;
    
    @Min(value = 1, message = "Thời hạn thẻ phải lớn hơn 0")
    private Integer validityMonths;
    
    private String benefits;
    
    private String color;
    
    private String icon;
    
    private Boolean isActive;
    
    private Integer displayOrder;
}
