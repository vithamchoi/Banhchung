package com.quannhabaninh.repository;

import com.quannhabaninh.entity.StorySection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorySectionRepository extends JpaRepository<StorySection, Long> {
    
    List<StorySection> findAllByOrderByDisplayOrderAsc();
    
    List<StorySection> findByIsActiveTrueOrderByDisplayOrderAsc();
    
    List<StorySection> findBySectionTypeOrderByDisplayOrderAsc(String sectionType);
    
    List<StorySection> findBySectionTypeAndIsActiveTrueOrderByDisplayOrderAsc(String sectionType);
}
