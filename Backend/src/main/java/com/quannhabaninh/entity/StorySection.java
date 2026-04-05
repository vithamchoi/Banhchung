package com.quannhabaninh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "story_sections")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorySection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 200)
    private String imageAlt;

    @Column(nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Section type: HEADER, STORY, PROCESS, CALL_TO_ACTION
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String sectionType = "STORY";

    // Optional subtitle or tag line
    @Column(length = 500)
    private String subtitle;

    // Optional quote or highlighted text
    @Column(columnDefinition = "TEXT")
    private String highlightedText;

    // Optional second image for dual-image sections
    @Column(length = 500)
    private String secondImageUrl;

    @Column(length = 200)
    private String secondImageAlt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
