package ptit.ttcs.phone.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ptit.ttcs.phone.dto.AdminProductResponse;
import ptit.ttcs.phone.dto.ProductRequest;
import ptit.ttcs.phone.service.AdminProductService;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class WarehouseProductController {

  private final AdminProductService adminProductService;

  @PreAuthorize("hasRole('WAREHOUSE_STAFF')")
  @PostMapping("/stockUpdate")
  public ResponseEntity<AdminProductResponse> updateProductStock(
      @RequestParam Integer productId,
      @RequestParam(defaultValue = "true") boolean increase,
      @RequestBody @Valid ProductRequest request) {
    return ResponseEntity.ok(adminProductService.updateProductStock(productId, request, increase));
  }
}
