package ptit.ttcs.phone.service;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ptit.ttcs.phone.dto.RatingModerationRequest;
import ptit.ttcs.phone.dto.RatingResponse;
import ptit.ttcs.phone.entity.Rating;
import ptit.ttcs.phone.exception.BadRequestException;
import ptit.ttcs.phone.exception.NotFoundException;
import ptit.ttcs.phone.repository.RatingRepository;

@Service
@RequiredArgsConstructor
public class RatingModerationService {
  private final RatingRepository ratingRepository;
  private final RedisTemplate<String, String> redisTemplate;

  @Transactional(readOnly = true)
  public Page<RatingResponse> getRatings(Pageable pageable, Boolean hidden) {
    Page<Rating> ratings = hidden == null
        ? ratingRepository.findAllByOrderByCreatedAtDesc(pageable)
        : ratingRepository.findByHiddenOrderByCreatedAtDesc(hidden, pageable);

    return ratings.map(this::mapToResponse);
  }

  @Transactional
  public RatingResponse moderateRating(Integer ratingId, RatingModerationRequest request) {
    Rating rating = ratingRepository.findByIdWithDetails(ratingId)
        .orElseThrow(() -> new NotFoundException("Rating not found"));

    if (Boolean.TRUE.equals(request.getHidden())
        && (request.getHideReason() == null || request.getHideReason().isBlank())) {
      throw new BadRequestException("Hide reason is required when hiding a rating");
    }

    rating.setHidden(request.getHidden());
    rating.setHideReason(Boolean.TRUE.equals(request.getHidden()) ? request.getHideReason().trim() : null);
    rating.setUpdatedAt(Instant.now());

    Rating savedRating = ratingRepository.save(rating);
    invalidateProductCache(savedRating.getProduct().getId());
    return mapToResponse(savedRating);
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
}
