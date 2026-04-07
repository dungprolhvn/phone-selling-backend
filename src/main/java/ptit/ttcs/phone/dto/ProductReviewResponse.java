package ptit.ttcs.phone.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductReviewResponse {
  private Byte star;
  private String comment;
  private Instant createdAt;
}
