package ptit.ttcs.phone.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ptit.ttcs.phone.dto.OrderItemResponse;
import ptit.ttcs.phone.dto.OrderStatusResponse;
import ptit.ttcs.phone.entity.Order;
import ptit.ttcs.phone.entity.OrderItem;
import ptit.ttcs.phone.exception.NotFoundException;
import ptit.ttcs.phone.repository.OrderItemRepository;
import ptit.ttcs.phone.repository.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderStatusService {
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;

  @Transactional(readOnly = true)
  public OrderStatusResponse lookupOrderStatus(String phone, Integer orderId) {
    Order order = orderRepository.findByIdAndUserPhone(orderId, phone)
        .orElseThrow(() -> new NotFoundException("Khong tim thay don hang"));

    List<OrderItemResponse> items = orderItemRepository.findByOrderIdWithProduct(orderId)
        .stream()
        .map(this::toItemResponse)
        .toList();

    String recipientName = null;
    String recipientPhone = null;
    String address = null;
    if (order.getShippingAddress() != null) {
      recipientName = order.getShippingAddress().getRecipientName();
      recipientPhone = order.getShippingAddress().getRecipientPhone();
      address = order.getShippingAddress().getAddress();
    }

    return new OrderStatusResponse(
        order.getId(),
        phone,
        order.getStatus(),
        order.getPaymentMethod(),
        order.getShipAtStore(),
        order.getTrackingNumber(),
        order.getCancelReason(),
        order.getTotalAmount(),
        order.getDiscountAmount(),
        order.getCreatedAt(),
        order.getUpdatedAt(),
        recipientName,
        recipientPhone,
        address,
        items
    );
  }

  private OrderItemResponse toItemResponse(OrderItem orderItem) {
    return OrderItemResponse.builder()
        .productId(orderItem.getProduct().getId())
        .productName(orderItem.getProduct().getName())
        .quantity(orderItem.getQuantity())
        .purchasedAtPrice(orderItem.getPurchasedAtPrice())
        .build();
  }
}
