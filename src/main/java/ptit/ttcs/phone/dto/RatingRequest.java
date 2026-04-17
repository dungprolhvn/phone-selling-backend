package ptit.ttcs.phone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingRequest {
  @NotNull(message = "Product ID is required")
  @JsonProperty("productId")
  private Integer productId;

  @NotNull(message = "Star rating is required")
  @Min(value = 1, message = "Star rating must be between 1 and 5")
  @Max(value = 5, message = "Star rating must be between 1 and 5")
  @JsonProperty("star")
  private Byte star;

  @Size(max = 5000, message = "Comment must not exceed 5000 characters")
  @JsonProperty("comment")
  private String comment;
}
