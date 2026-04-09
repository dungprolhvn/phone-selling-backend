package ptit.ttcs.phone.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ptit.ttcs.phone.dto.RatingModerationRequest;
import ptit.ttcs.phone.dto.RatingResponse;
import ptit.ttcs.phone.service.RatingModerationService;

@RestController
@RequestMapping("/api/admin/ratings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRatingController {
  private final RatingModerationService ratingModerationService;

  @GetMapping
  public ResponseEntity<Page<RatingResponse>> getRatings(
      Pageable pageable,
      @RequestParam(required = false) Boolean hidden) {
    return ResponseEntity.ok(ratingModerationService.getRatings(pageable, hidden));
  }

  @PatchMapping("/{ratingId}")
  public ResponseEntity<RatingResponse> moderateRating(
      @PathVariable Integer ratingId,
      @RequestBody @Valid RatingModerationRequest request) {
    return ResponseEntity.ok(ratingModerationService.moderateRating(ratingId, request));
  }
}
