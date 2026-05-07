package ptit.ttcs.phone.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyItemResponse {
  @JsonProperty("item")
  private OrderItemResponse item;

  @JsonProperty("warrantyEnd")
  private Instant warrantyEnd;
}
