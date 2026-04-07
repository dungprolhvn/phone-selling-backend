package ptit.ttcs.phone.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OrderStatusLookupRequest {
  @NotBlank(message = "Phone number is required")
  @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must contain exactly 10 digits")
  private String phone;

  @NotNull(message = "Order id is required")
  @Min(value = 1, message = "Order id must be greater than 0")
  private Integer orderId;
}
