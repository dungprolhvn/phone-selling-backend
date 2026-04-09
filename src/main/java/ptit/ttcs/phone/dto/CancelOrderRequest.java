package ptit.ttcs.phone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelOrderRequest {
  @NotBlank(message = "Cancel reason is required")
  @JsonProperty("cancelReason")
  private String cancelReason;
}
