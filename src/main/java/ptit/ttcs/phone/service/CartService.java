package ptit.ttcs.phone.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ptit.ttcs.phone.dto.Cart;
import ptit.ttcs.phone.document.ProductDocument;
import ptit.ttcs.phone.entity.Product;
import ptit.ttcs.phone.enums.ProductType;
import ptit.ttcs.phone.exception.BadRequestException;
import ptit.ttcs.phone.exception.ConflictException;
import ptit.ttcs.phone.repository.ProductRepository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartService {
  
  private final ProductRepository productRepository;
  private final ProductSearchService productSearchService;
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
  
  public Cart getCart(Authentication authentication, String guestId) {
    String redisKey = resolveRedisKey(authentication, guestId);
    try {
      String cartStr = redisTemplate.opsForValue().get(redisKey);
      return cartStr != null
          ? objectMapper.readValue(cartStr, Cart.class)
          : new Cart();
    } catch (Exception e) {
      throw new ConflictException(e.getMessage());
    }
  }
  
  private String resolveRedisKey(Authentication authentication, String guestId) {
    boolean isLoggedIn = authentication != null
        && authentication.isAuthenticated()
        && authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
    
    if (isLoggedIn) {
      Integer userId = (Integer) authentication.getPrincipal();
      return "cart:user:" + userId;
    }
    
    if (guestId == null || guestId.isBlank()) {
      throw new BadRequestException("Guest ID is required");
    }
    return "cart:guest:" + guestId;
  }

  public List<ProductDocument> getCartRecommendations(
      Authentication authentication,
      String guestId,
      int limit
  ) {
    int safeLimit = Math.max(1, Math.min(limit, 50));
    Cart cart = getCart(authentication, guestId);
    if (cart.getProducts() == null || cart.getProducts().isEmpty()) {
      return List.of();
    }

    List<Integer> productIds = new ArrayList<>(cart.getProducts().keySet());
    List<Product> cartProducts = productRepository.findAllById(productIds);
    List<Product> phones = cartProducts.stream()
        .filter(p -> p.getType() == ProductType.PHONE)
        .collect(Collectors.toList());

    if (phones.isEmpty()) {
      return List.of();
    }

    Set<Integer> excludeIds = new HashSet<>(productIds);
    List<ProductDocument> recommendations = new ArrayList<>();

    for (Product phone : phones) {
      List<ProductDocument> candidates = productSearchService.searchCrossSellForPhone(
          phone,
          safeLimit,
          new ArrayList<>(excludeIds)
      );

      for (ProductDocument doc : candidates) {
        if (doc == null) {
          continue;
        }
        Integer mysqlId = doc.getMysqlId();
        if (mysqlId != null && excludeIds.contains(mysqlId)) {
          continue;
        }
        recommendations.add(doc);
        if (mysqlId != null) {
          excludeIds.add(mysqlId);
        }
        if (recommendations.size() >= safeLimit) {
          break;
        }
      }

      if (recommendations.size() >= safeLimit) {
        break;
      }
    }

    return recommendations;
  }
}
