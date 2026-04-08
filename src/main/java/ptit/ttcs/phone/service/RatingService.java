package ptit.ttcs.phone.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ptit.ttcs.phone.dto.RatingRequest;
import ptit.ttcs.phone.dto.RatingResponse;
import ptit.ttcs.phone.entity.Rating;
import ptit.ttcs.phone.entity.OrderItem;
import ptit.ttcs.phone.entity.Product;
import ptit.ttcs.phone.entity.Account;
import ptit.ttcs.phone.exception.BadRequestException;
import ptit.ttcs.phone.exception.ForbiddenException;
import ptit.ttcs.phone.exception.NotFoundException;
import ptit.ttcs.phone.repository.RatingRepository;
import ptit.ttcs.phone.repository.OrderItemRepository;
import ptit.ttcs.phone.repository.ProductRepository;
import ptit.ttcs.phone.repository.AccountRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RatingService {
  
  private final RatingRepository ratingRepository;
  private final OrderItemRepository orderItemRepository;
  private final ProductRepository productRepository;
  private final AccountRepository accountRepository;
  
  @Transactional
  public RatingResponse createOrUpdateRating(Integer userId, RatingRequest request) {
    // Verify user exists
    Account user = accountRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));
    
    // Verify product exists
    Product product = productRepository.findById(request.getProductId())
        .orElseThrow(() -> new NotFoundException("Product not found"));
    
    // Verify user has purchased this product
    verifyUserPurchasedProduct(userId, request.getProductId());
    
    // Check if rating already exists
    Optional<Rating> existingRating = ratingRepository.findByUserIdAndProductId(userId, request.getProductId());
    
    Rating rating;
    if (existingRating.isPresent()) {
      // Update existing rating
      rating = existingRating.get();
      rating.setStar(request.getStar());
      rating.setComment(request.getComment());
      rating.setUpdatedAt(Instant.now());
      log.info("Updated rating {} for product {} by user {}", rating.getId(), request.getProductId(), userId);
    } else {
      // Create new rating
      rating = new Rating();
      rating.setUser(user);
      rating.setProduct(product);
      rating.setStar(request.getStar());
      rating.setComment(request.getComment());
      rating.setHidden(false);
      rating.setCreatedAt(Instant.now());
      rating.setUpdatedAt(Instant.now());
      log.info("Created rating for product {} by user {}", request.getProductId(), userId);
    }
    
    rating = ratingRepository.save(rating);
    return mapToResponse(rating);
  }
  
  @Transactional
  public void deleteRating(Integer userId, Integer ratingId) {
    Rating rating = ratingRepository.findByIdWithDetails(ratingId)
        .orElseThrow(() -> new NotFoundException("Rating not found"));
    
    // Verify ownership - only user who created rating can delete it
    if (!rating.getUser().getId().equals(userId)) {
      throw new ForbiddenException("You can only delete your own rating");
    }
    
    ratingRepository.delete(rating);
    log.info("Deleted rating {} by user {}", ratingId, userId);
  }
  
  public RatingResponse getRatingDetail(Integer ratingId) {
    Rating rating = ratingRepository.findByIdWithDetails(ratingId)
        .orElseThrow(() -> new NotFoundException("Rating not found"));
    
    return mapToResponse(rating);
  }
  
  private void verifyUserPurchasedProduct(Integer userId, Integer productId) {
    // Check if user has purchased this product with completed/delivered order status
    boolean hasPurchased = orderItemRepository.checkUserPurchasedProduct(userId, productId);
    
    if (!hasPurchased) {
      throw new BadRequestException("You can only rate products you have purchased");
    }
  }
  
  private RatingResponse mapToResponse(Rating rating) {
    return RatingResponse.builder()
        .ratingId(rating.getId())
        .productId(rating.getProduct().getId())
        .productName(rating.getProduct().getName())
        .userId(rating.getUser().getId())
        .userName(rating.getUser().getName())
        .star(rating.getStar())
        .comment(rating.getComment())
        .createdAt(rating.getCreatedAt())
        .updatedAt(rating.getUpdatedAt())
        .build();
  }
}
