package ptit.ttcs.phone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
  @Size(max = 1000, message = "Cancel reason must not exceed 1000 characters")
  @JsonProperty("cancelReason")
  private String cancelReason;
}
