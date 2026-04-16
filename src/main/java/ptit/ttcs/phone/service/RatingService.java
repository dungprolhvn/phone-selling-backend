package ptit.ttcs.phone.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ptit.ttcs.phone.dto.RatingRequest;
import ptit.ttcs.phone.dto.RatingResponse;
import ptit.ttcs.phone.entity.Account;
import ptit.ttcs.phone.entity.Product;
import ptit.ttcs.phone.entity.Rating;
import ptit.ttcs.phone.exception.BadRequestException;
import ptit.ttcs.phone.exception.ForbiddenException;
import ptit.ttcs.phone.exception.NotFoundException;
import ptit.ttcs.phone.repository.AccountRepository;
import ptit.ttcs.phone.repository.OrderItemRepository;
import ptit.ttcs.phone.repository.ProductRepository;
import ptit.ttcs.phone.repository.RatingRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class RatingService {
  private final RatingRepository ratingRepository;
  private final OrderItemRepository orderItemRepository;
  private final ProductRepository productRepository;
  private final AccountRepository accountRepository;
  private final RedisTemplate<String, String> redisTemplate;

  @Transactional
  public RatingResponse createOrUpdateRating(Integer userId, RatingRequest request) {
    Account user = accountRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));

    Product product = productRepository.findById(request.getProductId())
        .orElseThrow(() -> new NotFoundException("Product not found"));

    verifyUserPurchasedProduct(userId, request.getProductId());

    Optional<Rating> existingRating = ratingRepository.findByUserIdAndProductId(userId, request.getProductId());

    Rating rating;
    if (existingRating.isPresent()) {
      rating = existingRating.get();
      rating.setStar(request.getStar());
      rating.setComment(request.getComment());
      rating.setUpdatedAt(Instant.now());
      log.info("Updated rating {} for product {} by user {}", rating.getId(), request.getProductId(), userId);
    } else {
      rating = new Rating();
      rating.setUser(user);
      rating.setProduct(product);
      rating.setStar(request.getStar());
      rating.setComment(request.getComment());
      rating.setHidden(false);
      rating.setHideReason(null);
      rating.setCreatedAt(Instant.now());
      rating.setUpdatedAt(Instant.now());
      log.info("Created rating for product {} by user {}", request.getProductId(), userId);
    }

    rating = ratingRepository.save(rating);
    invalidateProductCache(rating.getProduct().getId());
    return mapToResponse(rating);
  }

  @Transactional
  public void deleteRating(Integer userId, Integer ratingId) {
    Rating rating = ratingRepository.findByIdWithDetails(ratingId)
        .orElseThrow(() -> new NotFoundException("Rating not found"));

    if (!rating.getUser().getId().equals(userId)) {
      throw new ForbiddenException("You can only delete your own rating");
    }

    Integer productId = rating.getProduct().getId();
    ratingRepository.delete(rating);
    invalidateProductCache(productId);
    log.info("Deleted rating {} by user {}", ratingId, userId);
  }

  @Transactional(readOnly = true)
  public RatingResponse getRatingDetail(Integer ratingId) {
    Rating rating = ratingRepository.findByIdWithDetails(ratingId)
        .orElseThrow(() -> new NotFoundException("Rating not found"));

    return mapToResponse(rating);
  }

  @Transactional(readOnly = true)
  public List<RatingResponse> getVisibleRatingsByProductId(Integer productId) {
    return ratingRepository.findByProductIdAndHiddenFalseOrderByCreatedAtDesc(productId)
        .stream()
        .map(this::mapToProductRatingResponse)
        .toList();
  }

  private void verifyUserPurchasedProduct(Integer userId, Integer productId) {
    boolean hasPurchased = orderItemRepository.checkUserPurchasedProduct(userId, productId);

    if (!hasPurchased) {
      throw new BadRequestException("You can only rate products you have purchased");
    }
  }

  private void invalidateProductCache(Integer productId) {
    redisTemplate.delete("product:" + productId);
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
        .hidden(rating.getHidden())
        .hideReason(rating.getHideReason())
        .createdAt(rating.getCreatedAt())
        .updatedAt(rating.getUpdatedAt())
        .build();
  }

  private RatingResponse mapToProductRatingResponse(Rating rating) {
    return RatingResponse.builder()
        .ratingId(rating.getId())
        .star(rating.getStar())
        .userName(rating.getUser().getName())
        .comment(rating.getComment())
        .createdAt(rating.getCreatedAt())
        .build();
  }
}
