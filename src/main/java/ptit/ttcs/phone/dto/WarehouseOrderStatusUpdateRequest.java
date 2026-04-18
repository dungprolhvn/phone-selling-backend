package ptit.ttcs.phone.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ptit.ttcs.phone.enums.OrderStatus;

@Data
public class WarehouseOrderStatusUpdateRequest {
  @NotNull(message = "orderId is required")
  private Integer orderId;

  @NotNull(message = "status is required")
  private OrderStatus status;

  @Size(max = 1000, message = "Cancel reason must not exceed 1000 characters")
  private String cancelReason;

  @AssertTrue(message = "status must be DELIVERYING, SUCCESS, or CANCELLED")
  public boolean isStatusAllowedForWarehouseUpdate() {
    return status == null
        || status == OrderStatus.DELIVERYING
        || status == OrderStatus.SUCCESS
        || status == OrderStatus.CANCELLED;
  }
}
