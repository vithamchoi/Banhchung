package com.quannhabaninh.service;

import com.quannhabaninh.dto.CreateProductRequest;
import com.quannhabaninh.dto.ProductListResponse;
import com.quannhabaninh.dto.UpdateProductRequest;
import com.quannhabaninh.entity.Product;
import com.quannhabaninh.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;
    
    // Lấy tất cả sản phẩm (không phân trang)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    // Lấy tất cả sản phẩm với sắp xếp
    public List<Product> getAllProducts(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isEmpty()) {
            return productRepository.findAll();
        }
        
        Sort sort = sortDirection.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
            
        return productRepository.findAll(sort);
    }
    
    // Lấy tất cả sản phẩm có phân trang và lọc
    public ProductListResponse getAllProductsPaginated(
            int page, 
            int size, 
            String sortBy, 
            String sortDirection,
            String category,
            Boolean isBestSeller,
            BigDecimal minPrice,
            BigDecimal maxPrice) {
        
        // Tạo Pageable với sắp xếp
        Pageable pageable;
        if (sortBy != null && !sortBy.isEmpty()) {
            Sort sort = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            pageable = PageRequest.of(page, size, sort);
        } else {
            pageable = PageRequest.of(page, size);
        }
        
        // Lấy dữ liệu từ database
        Page<Product> productPage = productRepository.findAll(pageable);
        List<Product> products = productPage.getContent();
        
        // Lọc theo các tiêu chí (nếu có)
        if (category != null || isBestSeller != null || minPrice != null || maxPrice != null) {
            products = products.stream()
                .filter(product -> category == null || product.getCategory().equalsIgnoreCase(category))
                .filter(product -> isBestSeller == null || product.getIsBestSeller().equals(isBestSeller))
                .filter(product -> minPrice == null || product.getPrice().compareTo(minPrice) >= 0)
                .filter(product -> maxPrice == null || product.getPrice().compareTo(maxPrice) <= 0)
                .collect(Collectors.toList());
        }
        
        // Tạo response
        ProductListResponse response = new ProductListResponse();
        response.setProducts(products);
        response.setCurrentPage(productPage.getNumber());
        response.setTotalPages(productPage.getTotalPages());
        response.setTotalItems(productPage.getTotalElements());
        response.setItemsPerPage(productPage.getSize());
        
        return response;
    }
    
    // Lấy sản phẩm theo ID
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    
    // Lấy sản phẩm theo category
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }
    
    // Lấy sản phẩm theo category với sắp xếp
    public List<Product> getProductsByCategory(String category, String sortBy, String sortDirection) {
        List<Product> products = productRepository.findByCategory(category);
        
        if (sortBy != null && !sortBy.isEmpty()) {
            products = products.stream()
                .sorted((p1, p2) -> {
                    int comparison = 0;
                    switch (sortBy.toLowerCase()) {
                        case "price":
                            comparison = p1.getPrice().compareTo(p2.getPrice());
                            break;
                        case "name":
                            comparison = p1.getName().compareToIgnoreCase(p2.getName());
                            break;
                        case "createdat":
                            comparison = p1.getCreatedAt().compareTo(p2.getCreatedAt());
                            break;
                        case "stockquantity":
                            comparison = p1.getStockQuantity().compareTo(p2.getStockQuantity());
                            break;
                        default:
                            comparison = 0;
                    }
                    return sortDirection.equalsIgnoreCase("desc") ? -comparison : comparison;
                })
                .collect(Collectors.toList());
        }
        
        return products;
    }
    
    // Lấy sản phẩm theo category có phân trang
    public ProductListResponse getProductsByCategoryPaginated(
            String category,
            int page,
            int size,
            String sortBy,
            String sortDirection,
            Boolean isBestSeller,
            BigDecimal minPrice,
            BigDecimal maxPrice) {
        
        // Lấy tất cả sản phẩm theo category
        List<Product> allProducts = productRepository.findByCategory(category);
        
        // Lọc theo các tiêu chí bổ sung
        List<Product> filteredProducts = allProducts.stream()
            .filter(product -> isBestSeller == null || product.getIsBestSeller().equals(isBestSeller))
            .filter(product -> minPrice == null || product.getPrice().compareTo(minPrice) >= 0)
            .filter(product -> maxPrice == null || product.getPrice().compareTo(maxPrice) <= 0)
            .collect(Collectors.toList());
        
        // Sắp xếp
        if (sortBy != null && !sortBy.isEmpty()) {
            filteredProducts = filteredProducts.stream()
                .sorted((p1, p2) -> {
                    int comparison = 0;
                    switch (sortBy.toLowerCase()) {
                        case "price":
                            comparison = p1.getPrice().compareTo(p2.getPrice());
                            break;
                        case "name":
                            comparison = p1.getName().compareToIgnoreCase(p2.getName());
                            break;
                        case "createdat":
                            comparison = p1.getCreatedAt().compareTo(p2.getCreatedAt());
                            break;
                        case "stockquantity":
                            comparison = p1.getStockQuantity().compareTo(p2.getStockQuantity());
                            break;
                        default:
                            comparison = 0;
                    }
                    return sortDirection.equalsIgnoreCase("desc") ? -comparison : comparison;
                })
                .collect(Collectors.toList());
        }
        
        // Tính toán phân trang
        int totalItems = filteredProducts.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalItems);
        
        // Lấy danh sách sản phẩm cho trang hiện tại
        List<Product> pageProducts = startIndex < totalItems 
            ? filteredProducts.subList(startIndex, endIndex) 
            : List.of();
        
        // Tạo response
        ProductListResponse response = new ProductListResponse();
        response.setProducts(pageProducts);
        response.setCurrentPage(page);
        response.setTotalPages(totalPages);
        response.setTotalItems(totalItems);
        response.setItemsPerPage(size);
        
        return response;
    }
    
    // Lấy sản phẩm best seller (Top 4 bán chạy nhất)
    public List<Product> getBestSellerProducts() {
        return productRepository.findTopBestSellersDynamic(PageRequest.of(0, 4));
    }
    
    // Tìm kiếm sản phẩm
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }
    
    // Tạo sản phẩm mới
    @Transactional
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }
    
    // Tạo sản phẩm mới với ảnh
    @Transactional
    public Product createProductWithImage(CreateProductRequest request, MultipartFile image, MultipartFile image2) throws IOException {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setIsBestSeller(request.getIsBestSeller() != null ? request.getIsBestSeller() : false);
        product.setIngredients(request.getIngredients());
        product.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
        if (request.getWeight() != null) product.setWeight(request.getWeight());
        
        if (image != null && !image.isEmpty()) {
            if (!fileStorageService.isImageFile(image)) throw new RuntimeException("File must be an image");
            product.setImage(fileStorageService.storeFile(image));
        }
        if (image2 != null && !image2.isEmpty()) {
            if (!fileStorageService.isImageFile(image2)) throw new RuntimeException("File 2 must be an image");
            product.setImage2(fileStorageService.storeFile(image2));
        }
        
        return productRepository.save(product);
    }
    
    // Cập nhật sản phẩm
    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        if (productDetails.getName() != null) product.setName(productDetails.getName());
        if (productDetails.getDescription() != null) product.setDescription(productDetails.getDescription());
        if (productDetails.getPrice() != null) product.setPrice(productDetails.getPrice());
        if (productDetails.getCategory() != null) product.setCategory(productDetails.getCategory());
        if (productDetails.getImage() != null) product.setImage(productDetails.getImage());
        if (productDetails.getImage2() != null) product.setImage2(productDetails.getImage2());
        if (productDetails.getIsBestSeller() != null) product.setIsBestSeller(productDetails.getIsBestSeller());
        if (productDetails.getIngredients() != null) product.setIngredients(productDetails.getIngredients());
        if (productDetails.getStockQuantity() != null) product.setStockQuantity(productDetails.getStockQuantity());
        if (productDetails.getWeight() != null) product.setWeight(productDetails.getWeight());
        
        return productRepository.save(product);
    }
    
    // Cập nhật sản phẩm với ảnh
    @Transactional
    public Product updateProductWithImage(Long id, UpdateProductRequest request, MultipartFile image, MultipartFile image2) throws IOException {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getCategory() != null) product.setCategory(request.getCategory());
        if (request.getIsBestSeller() != null) product.setIsBestSeller(request.getIsBestSeller());
        if (request.getIngredients() != null) product.setIngredients(request.getIngredients());
        if (request.getStockQuantity() != null) product.setStockQuantity(request.getStockQuantity());
        if (request.getWeight() != null) product.setWeight(request.getWeight());
        
        if (image != null && !image.isEmpty()) {
            if (!fileStorageService.isImageFile(image)) throw new RuntimeException("File must be an image");
            if (product.getImage() != null && !product.getImage().isEmpty()) fileStorageService.deleteFile(product.getImage());
            product.setImage(fileStorageService.storeFile(image));
        }
        if (image2 != null && !image2.isEmpty()) {
            if (!fileStorageService.isImageFile(image2)) throw new RuntimeException("File 2 must be an image");
            if (product.getImage2() != null && !product.getImage2().isEmpty()) fileStorageService.deleteFile(product.getImage2());
            product.setImage2(fileStorageService.storeFile(image2));
        }
        
        return productRepository.save(product);
    }
    
    // Cập nhật chỉ ảnh sản phẩm
    @Transactional
    public Product updateProductImage(Long id, MultipartFile image) throws IOException {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        // Kiểm tra file có phải là ảnh không
        if (!fileStorageService.isImageFile(image)) {
            throw new RuntimeException("File must be an image");
        }
        
        // Xóa ảnh cũ nếu có
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            fileStorageService.deleteFile(product.getImage());
        }
        
        // Lưu ảnh mới
        String imagePath = fileStorageService.storeFile(image);
        product.setImage(imagePath);
        
        return productRepository.save(product);
    }
    
    // Xóa sản phẩm
    @Transactional
    public void deleteProduct(Long id) {
        // Kiểm tra sản phẩm tồn tại
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        // Xóa ảnh nếu có
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            fileStorageService.deleteFile(product.getImage());
        }
        
        // Xóa sản phẩm
        productRepository.deleteById(id);
    }
    
    // Xóa nhiều sản phẩm
    @Transactional
    public int deleteMultipleProducts(List<Long> ids) {
        int deletedCount = 0;
        
        for (Long id : ids) {
            try {
                // Kiểm tra sản phẩm tồn tại
                Optional<Product> productOpt = productRepository.findById(id);
                
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    
                    // Xóa ảnh nếu có
                    if (product.getImage() != null && !product.getImage().isEmpty()) {
                        fileStorageService.deleteFile(product.getImage());
                    }
                    
                    // Xóa sản phẩm
                    productRepository.deleteById(id);
                    deletedCount++;
                }
            } catch (Exception e) {
                // Tiếp tục xóa các sản phẩm khác nếu có lỗi
                System.err.println("Error deleting product with id " + id + ": " + e.getMessage());
            }
        }
        
        return deletedCount;
    }
}
