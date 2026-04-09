package ptit.ttcs.phone.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingResponse {
  @JsonProperty("ratingId")
  private Integer ratingId;

  @JsonProperty("productId")
  private Integer productId;

  @JsonProperty("productName")
  private String productName;

  @JsonProperty("userId")
  private Integer userId;

  @JsonProperty("userName")
  private String userName;

  @JsonProperty("star")
  private Byte star;

  @JsonProperty("comment")
  private String comment;

  @JsonProperty("hidden")
  private Boolean hidden;

  @JsonProperty("hideReason")
  private String hideReason;

  @JsonProperty("createdAt")
  private Instant createdAt;

  @JsonProperty("updatedAt")
  private Instant updatedAt;
}
