package ptit.ttcs.phone.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrderRequest {
  @NotNull
  private Boolean shipAtStore = false;
  
  @NotNull
  private int shippingAddressId;
  
  @NotNull
  @Min(0)
  @Max(1)
  private int paymentMethod; // 0: online bank, 1: cod
  
  @Size(max = 50, message = "Ma giam gia khong hop le")
  private String promoCode;
}
