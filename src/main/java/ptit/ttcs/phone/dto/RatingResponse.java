package ptit.ttcs.phone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

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
  
  @JsonProperty("createdAt")
  private Instant createdAt;
  
  @JsonProperty("updatedAt")
  private Instant updatedAt;
}
