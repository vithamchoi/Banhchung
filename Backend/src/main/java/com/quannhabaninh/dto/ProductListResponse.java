package com.quannhabaninh.dto;

import com.quannhabaninh.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResponse {
    private List<Product> products;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int itemsPerPage;
}
