package ptit.ttcs.phone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShippingAddressRequest {
  @NotBlank(message = "Recipient name is required")
  private String recipientName;

  @NotBlank(message = "Recipient phone is required")
  @Pattern(regexp = "^[0-9]{10}$", message = "Recipient phone must contain exactly 10 digits")
  private String recipientPhone;

  @NotBlank(message = "Address is required")
  private String address;

  @NotNull(message = "isDefault is required")
  private Boolean isDefault;
}
