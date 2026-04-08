package ptit.ttcs.phone.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShippingAddressResponse {
  private Integer id;
  private String recipientName;
  private String recipientPhone;
  private String address;
  private Boolean isDefault;
  private Instant createdAt;
}
