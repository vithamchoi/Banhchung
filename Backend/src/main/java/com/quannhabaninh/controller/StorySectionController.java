package com.quannhabaninh.controller;

import com.quannhabaninh.dto.StorySectionRequest;
import com.quannhabaninh.dto.StorySectionResponse;
import com.quannhabaninh.service.FileStorageService;
import com.quannhabaninh.service.StorySectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/story-sections")

@RequiredArgsConstructor
public class StorySectionController {

    private final StorySectionService storySectionService;
    private final FileStorageService fileStorageService;

    /**
     * Upload image for story section (admin only)
     * POST /api/story-sections/upload-image
     */
    @PostMapping("/upload-image")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile image) {
        try {
            if (!fileStorageService.isImageFile(image)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "File không phải là ảnh hợp lệ"));
            }
            String imageUrl = fileStorageService.storeFile(image);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Không thể upload ảnh: " + e.getMessage()));
        }
    }

    /**
     * Get all story sections (public - only active)
     * GET /api/story-sections/public
     */
    @GetMapping("/public")
    public ResponseEntity<List<StorySectionResponse>> getPublicSections() {
        List<StorySectionResponse> sections = storySectionService.getActiveSections();
        return ResponseEntity.ok(sections);
    }

    /**
     * Get sections by type (public - only active)
     * GET /api/story-sections/public/type/{type}
     */
    @GetMapping("/public/type/{type}")
    public ResponseEntity<List<StorySectionResponse>> getPublicSectionsByType(@PathVariable String type) {
        List<StorySectionResponse> sections = storySectionService.getSectionsByType(type, true);
        return ResponseEntity.ok(sections);
    }

    /**
     * Get all story sections (admin - includes inactive)
     * GET /api/story-sections/admin
     */
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<StorySectionResponse>> getAllSections() {
        List<StorySectionResponse> sections = storySectionService.getAllSections();
        return ResponseEntity.ok(sections);
    }

    /**
     * Get sections by type (admin - includes inactive)
     * GET /api/story-sections/admin/type/{type}
     */
    @GetMapping("/admin/type/{type}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<StorySectionResponse>> getSectionsByType(
            @PathVariable String type,
            @RequestParam(required = false) Boolean activeOnly) {
        List<StorySectionResponse> sections = storySectionService.getSectionsByType(type, activeOnly);
        return ResponseEntity.ok(sections);
    }

    /**
     * Get section by ID
     * GET /api/story-sections/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<StorySectionResponse> getSectionById(@PathVariable Long id) {
        StorySectionResponse section = storySectionService.getSectionById(id);
        return ResponseEntity.ok(section);
    }

    /**
     * Create new story section (admin only)
     * POST /api/story-sections
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<StorySectionResponse> createSection(@Valid @RequestBody StorySectionRequest request) {
        StorySectionResponse section = storySectionService.createSection(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(section);
    }

    /**
     * Update story section (admin only)
     * PUT /api/story-sections/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<StorySectionResponse> updateSection(
            @PathVariable Long id,
            @Valid @RequestBody StorySectionRequest request) {
        StorySectionResponse section = storySectionService.updateSection(id, request);
        return ResponseEntity.ok(section);
    }

    /**
     * Delete story section (admin only)
     * DELETE /api/story-sections/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> deleteSection(@PathVariable Long id) {
        storySectionService.deleteSection(id);
        return ResponseEntity.ok(Map.of("message", "Xóa section thành công"));
    }

    /**
     * Toggle section active status (admin only)
     * PATCH /api/story-sections/{id}/toggle-status
     */
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<StorySectionResponse> toggleActiveStatus(@PathVariable Long id) {
        StorySectionResponse section = storySectionService.toggleActiveStatus(id);
        return ResponseEntity.ok(section);
    }

    /**
     * Reorder sections (admin only)
     * PUT /api/story-sections/reorder
     */
    @PutMapping("/reorder")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> reorderSections(@RequestBody List<Long> sectionIds) {
        storySectionService.reorderSections(sectionIds);
        return ResponseEntity.ok(Map.of("message", "Sắp xếp lại thứ tự thành công"));
    }
}

