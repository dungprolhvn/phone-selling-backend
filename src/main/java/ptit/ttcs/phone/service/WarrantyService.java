package ptit.ttcs.phone.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ptit.ttcs.phone.dto.OrderItemResponse;
import ptit.ttcs.phone.entity.Order;
import ptit.ttcs.phone.entity.OrderItem;
import ptit.ttcs.phone.enums.OrderStatus;
import ptit.ttcs.phone.enums.ProductType;
import ptit.ttcs.phone.exception.BadRequestException;
import ptit.ttcs.phone.exception.NotFoundException;
import ptit.ttcs.phone.repository.OrderItemRepository;
import ptit.ttcs.phone.repository.OrderRepository;

@Service
@RequiredArgsConstructor
public class WarrantyService {
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;

  @Transactional(readOnly = true)
  public ResponseEntity<Map<OrderItemResponse, Instant>> checkWarranty(int orderId) {
    Order order = orderRepository.findByIdWithDetails(orderId)
        .orElseThrow(() -> new NotFoundException("Khong tim thay don hang"));

    Instant fulfilledAt = order.getFulfilledAt();
    if (fulfilledAt == null || order.getStatus() != OrderStatus.SUCCESS) {
      throw new BadRequestException("Don hang chua hoan tat");
    }

    List<OrderItem> items = orderItemRepository.findByOrderIdWithProduct(orderId);
    Map<OrderItemResponse, Instant> result = new LinkedHashMap<>();
    for (OrderItem item : items) {
      Short warrantyMonth = item.getProduct().getWarrantyMonth();
      int warrantyMonths = warrantyMonth != null 
        ? warrantyMonth 
        : (item.getProduct().getType() == ProductType.PHONE ? 12 : 3);
      Instant warrantyEnd = fulfilledAt.plus(warrantyMonths, ChronoUnit.MONTHS);
      result.put(toItemResponse(item), warrantyEnd);
    }

    return ResponseEntity.ok(result);
  }

  private OrderItemResponse toItemResponse(OrderItem orderItem) {
    return OrderItemResponse.builder()
        .productId(orderItem.getProduct().getId())
        .productName(orderItem.getProduct().getName())
        .quantity(orderItem.getQuantity())
        .purchasedAtPrice(orderItem.getPurchasedAtPrice())
        .brandName(orderItem.getProduct().getBrand() != null ? orderItem.getProduct().getBrand().getName() : "N/A")
        .thumbnailUrl(orderItem.getProduct().getImageUrls() != null && !orderItem.getProduct().getImageUrls().isEmpty()
            ? orderItem.getProduct().getImageUrls().get(0)
            : null)
        .build();
  }
}
