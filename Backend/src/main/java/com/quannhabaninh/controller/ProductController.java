package com.quannhabaninh.controller;

import com.quannhabaninh.dto.CreateProductRequest;
import com.quannhabaninh.dto.ErrorResponse;
import com.quannhabaninh.dto.ProductListResponse;
import com.quannhabaninh.dto.UpdateProductRequest;
import com.quannhabaninh.entity.Product;
import com.quannhabaninh.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor

public class ProductController {
    
    private final ProductService productService;
    
    // GET /api/products - Lấy tất cả sản phẩm (không phân trang)
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
        List<Product> products = productService.getAllProducts(sortBy, sortDirection);
        return ResponseEntity.ok(products);
    }
    
    // GET /api/products/paginated - Lấy sản phẩm có phân trang
    @GetMapping("/paginated")
    public ResponseEntity<ProductListResponse> getAllProductsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isBestSeller,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        
        ProductListResponse response = productService.getAllProductsPaginated(
            page, size, sortBy, sortDirection, category, isBestSeller, minPrice, maxPrice);
        return ResponseEntity.ok(response);
    }
    
    // GET /api/products/{id} - Lấy sản phẩm theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            // Validate ID
            if (id == null || id <= 0) {
                ErrorResponse error = new ErrorResponse(
                    "Invalid ID",
                    "Product ID must be a positive number",
                    HttpStatus.BAD_REQUEST.value()
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            return productService.getProductById(id)
                .<ResponseEntity<?>>map(product -> ResponseEntity.ok(product))
                .orElseGet(() -> {
                    ErrorResponse error = new ErrorResponse(
                        "Product Not Found",
                        "Product with ID " + id + " does not exist",
                        HttpStatus.NOT_FOUND.value()
                    );
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
                });
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "Internal Server Error",
                "An error occurred while fetching product: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // GET /api/products/category/{category} - Lấy sản phẩm theo category (đơn giản)
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        try {
            // Validate category
            if (category == null || category.trim().isEmpty()) {
                ErrorResponse error = new ErrorResponse(
                    "Invalid Category",
                    "Category cannot be empty",
                    HttpStatus.BAD_REQUEST.value()
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            List<Product> products = productService.getProductsByCategory(category, sortBy, sortDirection);
            
            if (products.isEmpty()) {
                ErrorResponse error = new ErrorResponse(
                    "No Products Found",
                    "No products found in category: " + category,
                    HttpStatus.NOT_FOUND.value()
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "Internal Server Error",
                "An error occurred while fetching products: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // GET /api/products/category/{category}/paginated - Lấy sản phẩm theo category có phân trang
    @GetMapping("/category/{category}/paginated")
    public ResponseEntity<?> getProductsByCategoryPaginated(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) Boolean isBestSeller,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        try {
            // Validate category
            if (category == null || category.trim().isEmpty()) {
                ErrorResponse error = new ErrorResponse(
                    "Invalid Category",
                    "Category cannot be empty",
                    HttpStatus.BAD_REQUEST.value()
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            ProductListResponse response = productService.getProductsByCategoryPaginated(
                category, page, size, sortBy, sortDirection, isBestSeller, minPrice, maxPrice);
            
            if (response.getProducts().isEmpty()) {
                ErrorResponse error = new ErrorResponse(
                    "No Products Found",
                    "No products found in category: " + category,
                    HttpStatus.NOT_FOUND.value()
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "Internal Server Error",
                "An error occurred while fetching products: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // GET /api/products/bestsellers - Lấy sản phẩm best seller
    @GetMapping("/bestsellers")
    public ResponseEntity<List<Product>> getBestSellerProducts() {
        List<Product> products = productService.getBestSellerProducts();
        return ResponseEntity.ok(products);
    }
    
    // GET /api/products/search?q={keyword} - Tìm kiếm sản phẩm
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String q) {
        List<Product> products = productService.searchProducts(q);
        return ResponseEntity.ok(products);
    }
    
    // POST /api/products - Tạo sản phẩm mới (JSON)
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }
    
    // POST /api/products/with-image - Tạo sản phẩm mới với ảnh
    @PostMapping("/with-image")
    public ResponseEntity<?> createProductWithImage(
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "image2", required = false) MultipartFile image2,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("category") String category,
            @RequestParam(value = "isBestSeller", required = false, defaultValue = "false") Boolean isBestSeller,
            @RequestParam(value = "ingredients", required = false) String ingredients,
            @RequestParam(value = "stockQuantity", required = false, defaultValue = "0") Integer stockQuantity,
            @RequestParam(value = "weight", required = false) Integer weight) {
        try {
            if (name == null || name.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Product name is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Price must be greater than 0");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (category == null || category.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Category is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            CreateProductRequest request = new CreateProductRequest();
            request.setName(name);
            request.setDescription(description);
            request.setPrice(price);
            request.setCategory(category);
            request.setIsBestSeller(isBestSeller);
            request.setIngredients(ingredients);
            request.setStockQuantity(stockQuantity);
            request.setWeight(weight);
            
            Product createdProduct = productService.createProductWithImage(request, image, image2);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create product: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // PUT /api/products/{id} - Cập nhật sản phẩm (JSON)
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product productDetails) {
        try {
            Product updatedProduct = productService.updateProduct(id, productDetails);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // PUT /api/products/{id}/with-image - Cập nhật sản phẩm với ảnh
    @PutMapping("/{id}/with-image")
    public ResponseEntity<?> updateProductWithImage(
            @PathVariable Long id,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "image2", required = false) MultipartFile image2,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("category") String category,
            @RequestParam(value = "isBestSeller", required = false, defaultValue = "false") Boolean isBestSeller,
            @RequestParam(value = "ingredients", required = false) String ingredients,
            @RequestParam(value = "stockQuantity", required = false, defaultValue = "0") Integer stockQuantity,
            @RequestParam(value = "weight", required = false) Integer weight) {
        try {
            UpdateProductRequest request = new UpdateProductRequest();
            request.setName(name);
            request.setDescription(description);
            request.setPrice(price);
            request.setCategory(category);
            request.setIsBestSeller(isBestSeller);
            request.setIngredients(ingredients);
            request.setStockQuantity(stockQuantity);
            request.setWeight(weight);
            
            Product updatedProduct = productService.updateProductWithImage(id, request, image, image2);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update product: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // PATCH /api/products/{id}/image - Cập nhật chỉ ảnh sản phẩm
    @PatchMapping("/{id}/image")
    public ResponseEntity<?> updateProductImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image) {
        try {
            Product updatedProduct = productService.updateProductImage(id, image);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update product image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // DELETE /api/products/{id} - Xóa sản phẩm
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            // Validate ID
            if (id == null || id <= 0) {
                ErrorResponse error = new ErrorResponse(
                    "Invalid ID",
                    "Product ID must be a positive number",
                    HttpStatus.BAD_REQUEST.value()
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            productService.deleteProduct(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Product deleted successfully");
            response.put("id", id.toString());
            
            return ResponseEntity.ok(response);
        } catch (DataIntegrityViolationException e) {
            ErrorResponse error = new ErrorResponse(
                "Cannot Delete Product",
                "Product is referenced by other records (e.g. orders, cart items).",
                HttpStatus.CONFLICT.value()
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (RuntimeException e) {
            ErrorResponse error = new ErrorResponse(
                "Product Not Found",
                e.getMessage(),
                HttpStatus.NOT_FOUND.value()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "Internal Server Error",
                "An error occurred while deleting product: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // DELETE /api/products - Xóa nhiều sản phẩm
    @DeleteMapping
    public ResponseEntity<?> deleteMultipleProducts(@RequestParam List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                ErrorResponse error = new ErrorResponse(
                    "Invalid Request",
                    "Product IDs list cannot be empty",
                    HttpStatus.BAD_REQUEST.value()
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            int deletedCount = productService.deleteMultipleProducts(ids);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Products deleted successfully");
            response.put("deletedCount", deletedCount);
            response.put("requestedCount", ids.size());
            
            return ResponseEntity.ok(response);
        } catch (DataIntegrityViolationException e) {
            ErrorResponse error = new ErrorResponse(
                "Cannot Delete Products",
                "One or more products are referenced by other records (e.g. orders, cart items).",
                HttpStatus.CONFLICT.value()
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "Internal Server Error",
                "An error occurred while deleting products: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
