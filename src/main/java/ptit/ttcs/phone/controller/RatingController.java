package ptit.ttcs.phone.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import ptit.ttcs.phone.dto.RatingRequest;
import ptit.ttcs.phone.dto.RatingResponse;
import ptit.ttcs.phone.service.RatingService;

@RestController
@RequestMapping("/api/ratings")
@Slf4j
@RequiredArgsConstructor
public class RatingController {
  
  private final RatingService ratingService;
  
  @PreAuthorize("hasRole('USER')")
  @PostMapping
  public ResponseEntity<RatingResponse> createOrUpdateRating(
    Authentication authentication,
    @RequestBody @Valid RatingRequest request
  ) {
    int userId = (int) authentication.getPrincipal();
    RatingResponse response = ratingService.createOrUpdateRating(userId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
  
  @PreAuthorize("hasRole('USER')")
  @DeleteMapping("/{ratingId}")
  public ResponseEntity<Void> deleteRating(
    Authentication authentication,
    @PathVariable Integer ratingId
  ) {
    int userId = (int) authentication.getPrincipal();
    ratingService.deleteRating(userId, ratingId);
    return ResponseEntity.noContent().build();
  }
  
  @GetMapping("/{ratingId}")
  public ResponseEntity<RatingResponse> getRatingDetail(
    @PathVariable Integer ratingId
  ) {
    RatingResponse response = ratingService.getRatingDetail(ratingId);
    return ResponseEntity.ok(response);
  }
}
