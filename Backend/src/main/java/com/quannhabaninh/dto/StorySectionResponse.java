package com.quannhabaninh.dto;

import com.quannhabaninh.entity.StorySection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorySectionResponse {
    
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private String imageAlt;
    private Integer displayOrder;
    private Boolean isActive;
    private String sectionType;
    private String subtitle;
    private String highlightedText;
    private String secondImageUrl;
    private String secondImageAlt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StorySectionResponse fromEntity(StorySection section) {
        return StorySectionResponse.builder()
                .id(section.getId())
                .title(section.getTitle())
                .content(section.getContent())
                .imageUrl(section.getImageUrl())
                .imageAlt(section.getImageAlt())
                .displayOrder(section.getDisplayOrder())
                .isActive(section.getIsActive())
                .sectionType(section.getSectionType())
                .subtitle(section.getSubtitle())
                .highlightedText(section.getHighlightedText())
                .secondImageUrl(section.getSecondImageUrl())
                .secondImageAlt(section.getSecondImageAlt())
                .createdAt(section.getCreatedAt())
                .updatedAt(section.getUpdatedAt())
                .build();
    }
}
