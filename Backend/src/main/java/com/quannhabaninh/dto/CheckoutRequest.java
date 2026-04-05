package com.quannhabaninh.dto;

import com.quannhabaninh.entity.PaymentMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    @NotBlank(message = "Shipping name is required")
    private String shippingName;
    
    @NotBlank(message = "Shipping phone is required")
    private String shippingPhone;
    
    @Email(message = "Invalid email format")
    private String shippingEmail;
    
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;
    
    private String shippingCity;
    
    private String shippingDistrict;
    
    private String shippingWard;
    
    private String notes;
    
    private BigDecimal discountAmount;
    
    private BigDecimal shippingFee;
}
