package com.quannhabaninh.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    
    @Value("${file.upload-dir:uploads/products}")
    private String uploadDir;
    
    public String storeFile(MultipartFile file) throws IOException {
        // Tạo thư mục nếu chưa tồn tại
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Lấy tên file gốc và extension
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // Tạo tên file mới với UUID để tránh trùng lặp
        String newFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Lưu file
        Path targetLocation = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        // Trả về đường dẫn để frontend có thể load qua WebConfig (/uploads/**)
        return "/uploads/" + newFilename;
    }
    
    public void deleteFile(String filePath) {
        try {
            if (filePath != null && !filePath.isEmpty()) {
                // filePath is like "/uploads/abc.jpg", but the real file is in uploadDir/abc.jpg
                String filename = filePath.startsWith("/uploads/") 
                    ? filePath.substring("/uploads/".length()) 
                    : Paths.get(filePath).getFileName().toString();
                Path realPath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(filename);
                Files.deleteIfExists(realPath);
            }
        } catch (IOException e) {
            System.err.println("Could not delete file: " + filePath);
        }
    }
    
    public boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
}
