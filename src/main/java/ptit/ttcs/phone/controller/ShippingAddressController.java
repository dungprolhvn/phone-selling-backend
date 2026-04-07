package ptit.ttcs.phone.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ptit.ttcs.phone.dto.ShippingAddressRequest;
import ptit.ttcs.phone.dto.ShippingAddressResponse;
import ptit.ttcs.phone.service.ShippingAddressService;

@RestController
@RequestMapping("/api/users/addresses")
@RequiredArgsConstructor
public class ShippingAddressController {
  private final ShippingAddressService shippingAddressService;

  @GetMapping
  public ResponseEntity<List<ShippingAddressResponse>> getAddresses(Authentication authentication) {
    Integer userId = (Integer) authentication.getPrincipal();
    return ResponseEntity.ok(shippingAddressService.getAddresses(userId));
  }

  @PostMapping
  public ResponseEntity<List<ShippingAddressResponse>> createAddress(
      Authentication authentication,
      @RequestBody @Valid ShippingAddressRequest request) {
    Integer userId = (Integer) authentication.getPrincipal();
    return ResponseEntity.ok(shippingAddressService.createAddress(userId, request));
  }

  @PutMapping("/{addressId}")
  public ResponseEntity<List<ShippingAddressResponse>> updateAddress(
      Authentication authentication,
      @PathVariable Integer addressId,
      @RequestBody @Valid ShippingAddressRequest request) {
    Integer userId = (Integer) authentication.getPrincipal();
    return ResponseEntity.ok(shippingAddressService.updateAddress(userId, addressId, request));
  }

  @DeleteMapping("/{addressId}")
  public ResponseEntity<List<ShippingAddressResponse>> deleteAddress(
      Authentication authentication,
      @PathVariable Integer addressId) {
    Integer userId = (Integer) authentication.getPrincipal();
    return ResponseEntity.ok(shippingAddressService.deleteAddress(userId, addressId));
  }
}
