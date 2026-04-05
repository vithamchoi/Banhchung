package com.quannhabaninh.repository;

import com.quannhabaninh.entity.MembershipCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembershipCardRepository extends JpaRepository<MembershipCard, Long> {
    
    List<MembershipCard> findAllByOrderByDisplayOrderAsc();
    
    List<MembershipCard> findByIsActiveTrueOrderByDisplayOrderAsc();
    
    boolean existsByName(String name);
    
    boolean existsByNameAndIdNot(String name, Long id);
}
