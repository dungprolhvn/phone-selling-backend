package ptit.ttcs.phone.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ptit.ttcs.phone.enums.DiscountType;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
public class PromoCreationRequest {
  @Size(max = 255)
  @NotBlank
  private String name;
  
  @NotBlank
  @Size()
  private String description;
  
  @NotNull
  private Instant startDate;
  
  @NotNull
  private Instant endDate;
  
  @NotNull
  @Enumerated(EnumType.STRING)
  private DiscountType discountType;
  
  @NotNull
  private double discountValue;
  
  @NotBlank
  @Size(min = 5, max = 50)
  private String voucherCode;
  
  @NotNull
  @Min(1)
  private int usageLimit;
  
  @NotNull
  @Size(min = 1)
  private Map<String, Object> promoImageUrls;
  
  @NotNull
  @Size(min = 1)
  private List<Integer> productIds;
}
