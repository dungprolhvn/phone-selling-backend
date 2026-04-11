package ptit.ttcs.phone.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class StatRequest {
  @NotNull
  private Instant startDate;

  @NotNull
  private Instant endDate;
}
