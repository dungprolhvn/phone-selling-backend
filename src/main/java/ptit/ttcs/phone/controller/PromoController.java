package ptit.ttcs.phone.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ptit.ttcs.phone.dto.PromoCreationRequest;
import ptit.ttcs.phone.entity.Promo;
import ptit.ttcs.phone.service.PromoService;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@Slf4j
@RequiredArgsConstructor
public class PromoController {
  
  private final PromoService promoService;
  
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<List<Promo>> getAllPromos() {
    return ResponseEntity.ok(promoService.getAllPromos());
  }
  
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/create")
  public ResponseEntity<Void> createPromo(@RequestBody @Valid PromoCreationRequest request) {
    return ResponseEntity.ok(promoService.createPromo(request));
  }
  
}
