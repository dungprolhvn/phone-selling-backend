package ptit.ttcs.phone.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ptit.ttcs.phone.dto.Cart;
import ptit.ttcs.phone.service.CartService;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
  
  private final CartService cartService;
  
  @PostMapping("/update")
  public ResponseEntity<Void> addToCart(
      @RequestBody @Valid Cart request,
      @RequestHeader(value = "X-Guest-Id", required = false) String guestId,
      Authentication authentication) throws Exception {
    boolean isLoggedIn = authentication != null
        && authentication.isAuthenticated()
        && authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
    if (isLoggedIn) {
      cartService.addToCart(request, authentication);
    }
    else {
      cartService.addToCartGuest(request, guestId);
    }
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
