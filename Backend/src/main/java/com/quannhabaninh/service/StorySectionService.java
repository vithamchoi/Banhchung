package com.quannhabaninh.service;

import com.quannhabaninh.dto.StorySectionRequest;
import com.quannhabaninh.dto.StorySectionResponse;
import com.quannhabaninh.entity.StorySection;
import com.quannhabaninh.repository.StorySectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorySectionService {

    private final StorySectionRepository storySectionRepository;

    /**
     * Get all story sections (admin view - includes inactive)
     */
    public List<StorySectionResponse> getAllSections() {
        log.info("Fetching all story sections");
        return storySectionRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(StorySectionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get only active story sections (public view)
     */
    public List<StorySectionResponse> getActiveSections() {
        log.info("Fetching active story sections");
        return storySectionRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(StorySectionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get sections by type
     */
    public List<StorySectionResponse> getSectionsByType(String sectionType, Boolean activeOnly) {
        log.info("Fetching story sections by type: {}, activeOnly: {}", sectionType, activeOnly);
        List<StorySection> sections;
        
        if (activeOnly != null && activeOnly) {
            sections = storySectionRepository.findBySectionTypeAndIsActiveTrueOrderByDisplayOrderAsc(sectionType);
        } else {
            sections = storySectionRepository.findBySectionTypeOrderByDisplayOrderAsc(sectionType);
        }
        
        return sections.stream()
                .map(StorySectionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get section by ID
     */
    public StorySectionResponse getSectionById(Long id) {
        log.info("Fetching story section by id: {}", id);
        StorySection section = storySectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy section với ID: " + id));
        return StorySectionResponse.fromEntity(section);
    }

    /**
     * Create new story section
     */
    @Transactional
    public StorySectionResponse createSection(StorySectionRequest request) {
        log.info("Creating new story section: {}", request.getTitle());
        
        StorySection section = StorySection.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .imageAlt(request.getImageAlt())
                .displayOrder(request.getDisplayOrder())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .sectionType(request.getSectionType())
                .subtitle(request.getSubtitle())
                .highlightedText(request.getHighlightedText())
                .secondImageUrl(request.getSecondImageUrl())
                .secondImageAlt(request.getSecondImageAlt())
                .build();

        StorySection savedSection = storySectionRepository.save(section);
        log.info("Created story section with id: {}", savedSection.getId());
        
        return StorySectionResponse.fromEntity(savedSection);
    }

    /**
     * Update existing story section
     */
    @Transactional
    public StorySectionResponse updateSection(Long id, StorySectionRequest request) {
        log.info("Updating story section id: {}", id);
        
        StorySection section = storySectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy section với ID: " + id));

        section.setTitle(request.getTitle());
        section.setContent(request.getContent());
        section.setImageUrl(request.getImageUrl());
        section.setImageAlt(request.getImageAlt());
        section.setDisplayOrder(request.getDisplayOrder());
        section.setIsActive(request.getIsActive() != null ? request.getIsActive() : section.getIsActive());
        section.setSectionType(request.getSectionType());
        section.setSubtitle(request.getSubtitle());
        section.setHighlightedText(request.getHighlightedText());
        section.setSecondImageUrl(request.getSecondImageUrl());
        section.setSecondImageAlt(request.getSecondImageAlt());

        StorySection updatedSection = storySectionRepository.save(section);
        log.info("Updated story section id: {}", id);
        
        return StorySectionResponse.fromEntity(updatedSection);
    }

    /**
     * Delete story section
     */
    @Transactional
    public void deleteSection(Long id) {
        log.info("Deleting story section id: {}", id);
        
        if (!storySectionRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy section với ID: " + id);
        }

        storySectionRepository.deleteById(id);
        log.info("Deleted story section id: {}", id);
    }

    /**
     * Toggle section active status
     */
    @Transactional
    public StorySectionResponse toggleActiveStatus(Long id) {
        log.info("Toggling active status for story section id: {}", id);
        
        StorySection section = storySectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy section với ID: " + id));

        section.setIsActive(!section.getIsActive());
        StorySection updatedSection = storySectionRepository.save(section);
        
        log.info("Toggled active status for section id: {} to {}", id, updatedSection.getIsActive());
        return StorySectionResponse.fromEntity(updatedSection);
    }

    /**
     * Reorder sections
     */
    @Transactional
    public void reorderSections(List<Long> sectionIds) {
        log.info("Reordering {} story sections", sectionIds.size());
        
        for (int i = 0; i < sectionIds.size(); i++) {
            Long sectionId = sectionIds.get(i);
            StorySection section = storySectionRepository.findById(sectionId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy section với ID: " + sectionId));
            section.setDisplayOrder(i + 1);
            storySectionRepository.save(section);
        }
        
        log.info("Reordered story sections successfully");
    }
}
