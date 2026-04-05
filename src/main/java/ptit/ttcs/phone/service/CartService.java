package ptit.ttcs.phone.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ptit.ttcs.phone.dto.Cart;
import ptit.ttcs.phone.entity.Product;
import ptit.ttcs.phone.exception.BadRequestException;
import ptit.ttcs.phone.exception.ConflictException;
import ptit.ttcs.phone.repository.ProductRepository;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartService {
  
  private final ProductRepository productRepository;
  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;
  
  public void addToCart(
      @Valid Cart request,
      Authentication authentication) throws Exception {
    // get cart
    Integer userId = (Integer) authentication.getPrincipal();
    Cart cart;
    try {
      String cartStr = redisTemplate.opsForValue().get(String.format("cart:user:%s", userId));
      if (cartStr != null) {
        cart = objectMapper.readValue(cartStr, Cart.class);
      }
      else {
        // Cart does not exist, create a new one
        cart = new Cart();
      }
    }
    catch (IllegalArgumentException iae) {
      throw new BadRequestException("Invalid argument: " + iae.getMessage());
    }
    catch (Exception e) {
      throw new ConflictException(e.getMessage());
    }
    // update cart
    try {
      request.getProducts().forEach((productId, count) -> {
        Product p = productRepository.getProductById(productId)
            .orElseThrow(() -> new BadRequestException("San pham khong ton tai"));
        cart.update(productId, count);
      });
    }
    catch (IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage());
    }
    // save cart
    String cartJson = objectMapper.writeValueAsString(cart);
    redisTemplate.opsForValue().set(String.format("cart:user:%s", userId), cartJson, Duration.ofDays(30));
  }
  
  public void addToCartGuest(@Valid Cart request, String guestId) throws Exception {
    if (guestId == null) {
      throw new BadRequestException("Khong tim thay guestId de tao gio hang");
    }
    Cart cart;
    try {
      String cartStr = redisTemplate.opsForValue().get(String.format("cart:guest:%s", guestId));
      if (cartStr != null) {
        cart = objectMapper.readValue(cartStr, Cart.class);
      }
      else {
        // Cart does not exist, create a new one
        cart = new Cart();
      }
    }
    catch (IllegalArgumentException iae) {
      throw new BadRequestException("Invalid argument: " + iae.getMessage());
    }
    catch (Exception e) {
      throw new ConflictException(e.getMessage());
    }
    // update cart
    try {
      request.getProducts().forEach((productId, count) -> {
        Product p = productRepository.getProductById(productId)
            .orElseThrow(() -> new BadRequestException("San pham khong ton tai"));
        cart.update(productId, count);
      });
    }
    catch (IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage());
    }
    // save cart
    String cartJson = objectMapper.writeValueAsString(cart);
    redisTemplate.opsForValue().set(String.format("cart:guest:%s", guestId), cartJson, Duration.ofDays(7));
  }
}
