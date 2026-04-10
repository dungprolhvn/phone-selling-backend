package ptit.ttcs.phone.dto;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProductResponse {
  private Integer id;
  private String name;
  private String type;
  private BigDecimal basePrice;
  private Integer brandId;
  private Integer stockAvailable;
  private Instant releaseDate;
  private Instant updatedAt;
}
