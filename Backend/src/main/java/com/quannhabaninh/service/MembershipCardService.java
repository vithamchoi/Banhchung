package com.quannhabaninh.service;

import com.quannhabaninh.dto.CardResponse;
import com.quannhabaninh.dto.CreateCardRequest;
import com.quannhabaninh.dto.UpdateCardRequest;
import com.quannhabaninh.entity.MembershipCard;
import com.quannhabaninh.repository.MembershipCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MembershipCardService {
    
    private final MembershipCardRepository cardRepository;
    
    public List<CardResponse> getAllCards() {
        return cardRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<CardResponse> getActiveCards() {
        return cardRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public CardResponse getCardById(Long id) {
        MembershipCard card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thẻ với ID: " + id));
        return mapToResponse(card);
    }
    
    @Transactional
    public CardResponse createCard(CreateCardRequest request) {
        // Validate unique name
        if (cardRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên thẻ đã tồn tại: " + request.getName());
        }
        
        // Validate discount percentage
        if (request.getDiscountPercentage() != null && 
            (request.getDiscountPercentage() < 0 || request.getDiscountPercentage() > 100)) {
            throw new RuntimeException("Phần trăm giảm giá phải từ 0 đến 100");
        }
        
        MembershipCard card = MembershipCard.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountPercentage(request.getDiscountPercentage())
                .validityMonths(request.getValidityMonths())
                .benefits(request.getBenefits())
                .color(request.getColor())
                .icon(request.getIcon())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();
        
        MembershipCard savedCard = cardRepository.save(card);
        return mapToResponse(savedCard);
    }
    
    @Transactional
    public CardResponse updateCard(Long id, UpdateCardRequest request) {
        MembershipCard card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thẻ với ID: " + id));
        
        // Validate unique name (excluding current card)
        if (cardRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new RuntimeException("Tên thẻ đã tồn tại: " + request.getName());
        }
        
        // Validate discount percentage
        if (request.getDiscountPercentage() != null && 
            (request.getDiscountPercentage() < 0 || request.getDiscountPercentage() > 100)) {
            throw new RuntimeException("Phần trăm giảm giá phải từ 0 đến 100");
        }
        
        card.setName(request.getName());
        card.setDescription(request.getDescription());
        card.setPrice(request.getPrice());
        card.setDiscountPercentage(request.getDiscountPercentage());
        card.setValidityMonths(request.getValidityMonths());
        card.setBenefits(request.getBenefits());
        card.setColor(request.getColor());
        card.setIcon(request.getIcon());
        card.setIsActive(request.getIsActive() != null ? request.getIsActive() : card.getIsActive());
        card.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : card.getDisplayOrder());
        
        MembershipCard updatedCard = cardRepository.save(card);
        return mapToResponse(updatedCard);
    }
    
    @Transactional
    public void deleteCard(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy thẻ với ID: " + id);
        }
        cardRepository.deleteById(id);
    }
    
    @Transactional
    public CardResponse toggleCardStatus(Long id) {
        MembershipCard card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thẻ với ID: " + id));
        
        card.setIsActive(!card.getIsActive());
        MembershipCard updatedCard = cardRepository.save(card);
        return mapToResponse(updatedCard);
    }
    
    private CardResponse mapToResponse(MembershipCard card) {
        return CardResponse.builder()
                .id(card.getId())
                .name(card.getName())
                .description(card.getDescription())
                .price(card.getPrice())
                .discountPercentage(card.getDiscountPercentage())
                .validityMonths(card.getValidityMonths())
                .benefits(card.getBenefits())
                .color(card.getColor())
                .icon(card.getIcon())
                .isActive(card.getIsActive())
                .displayOrder(card.getDisplayOrder())
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .build();
    }
}
