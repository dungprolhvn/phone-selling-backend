package ptit.ttcs.phone.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ptit.ttcs.phone.dto.AdminProductResponse;
import ptit.ttcs.phone.dto.ProductRequest;
import ptit.ttcs.phone.service.AdminProductService;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class AdminProductController {

  private final AdminProductService adminProductService;

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<AdminProductResponse> createProduct(@RequestBody @Valid ProductRequest request) {
    return ResponseEntity.ok(adminProductService.createProduct(request));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/infoUpdate")
  public ResponseEntity<AdminProductResponse> updateProductInfo(
      @RequestParam Integer productId,
      @RequestBody @Valid ProductRequest request) {
    return ResponseEntity.ok(adminProductService.updateProductInfo(productId, request));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/infoUpdate/{productId}")
  public ResponseEntity<AdminProductResponse> updateProductInfoByPath(
      @PathVariable Integer productId,
      @RequestBody @Valid ProductRequest request) {
    return ResponseEntity.ok(adminProductService.updateProductInfo(productId, request));
  }
}
