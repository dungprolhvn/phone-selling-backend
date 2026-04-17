package ptit.ttcs.phone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class RatingModerationRequest {
  @NotNull(message = "Hidden flag is required")
  @JsonProperty("hidden")
  private Boolean hidden;

  @Size(max = 1000, message = "Hide reason must not exceed 1000 characters")
  @JsonProperty("hideReason")
  private String hideReason;
}
