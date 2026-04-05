package com.quannhabaninh.controller;

import com.quannhabaninh.dto.CardResponse;
import com.quannhabaninh.dto.CreateCardRequest;
import com.quannhabaninh.dto.ErrorResponse;
import com.quannhabaninh.dto.UpdateCardRequest;
import com.quannhabaninh.service.MembershipCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")

@RequiredArgsConstructor
public class MembershipCardController {
    
    private final MembershipCardService cardService;
    
    // GET /api/cards - Lấy tất cả thẻ (Public)
    @GetMapping
    public ResponseEntity<List<CardResponse>> getAllCards() {
        List<CardResponse> cards = cardService.getAllCards();
        return ResponseEntity.ok(cards);
    }
    
    // GET /api/cards/active - Lấy các thẻ đang hoạt động (Public)
    @GetMapping("/active")
    public ResponseEntity<List<CardResponse>> getActiveCards() {
        List<CardResponse> cards = cardService.getActiveCards();
        return ResponseEntity.ok(cards);
    }
    
    // GET /api/cards/{id} - Lấy thẻ theo ID (Public)
    @GetMapping("/{id}")
    public ResponseEntity<?> getCardById(@PathVariable Long id) {
        try {
            CardResponse card = cardService.getCardById(id);
            return ResponseEntity.ok(card);
        } catch (RuntimeException e) {
            ErrorResponse error = new ErrorResponse(
                "Card Not Found",
                e.getMessage(),
                HttpStatus.NOT_FOUND.value()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    // POST /api/cards - Tạo thẻ mới (Admin only)
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createCard(@Valid @RequestBody CreateCardRequest request) {
        try {
            CardResponse card = cardService.createCard(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(card);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    // PUT /api/cards/{id} - Cập nhật thẻ (Admin only)
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateCard(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCardRequest request) {
        try {
            CardResponse card = cardService.updateCard(id, request);
            return ResponseEntity.ok(card);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    // DELETE /api/cards/{id} - Xóa thẻ (Admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteCard(@PathVariable Long id) {
        try {
            cardService.deleteCard(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Xóa thẻ thành công");
            response.put("id", id.toString());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ErrorResponse error = new ErrorResponse(
                "Card Not Found",
                e.getMessage(),
                HttpStatus.NOT_FOUND.value()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    // PATCH /api/cards/{id}/toggle-status - Bật/Tắt trạng thái thẻ (Admin only)
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> toggleCardStatus(@PathVariable Long id) {
        try {
            CardResponse card = cardService.toggleCardStatus(id);
            return ResponseEntity.ok(card);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}
