package ptit.ttcs.phone.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ptit.ttcs.phone.dto.OrderItemDetailResponse;
import ptit.ttcs.phone.dto.PurchaseHistoryItemResponse;
import ptit.ttcs.phone.dto.PurchaseHistoryResponse;
import ptit.ttcs.phone.entity.Order;
import ptit.ttcs.phone.entity.OrderItem;
import ptit.ttcs.phone.repository.OrderItemRepository;
import ptit.ttcs.phone.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderHistoryService {
  
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  
  public PurchaseHistoryResponse getPurchaseHistory(Integer userId, Pageable pageable) {
    Page<Order> orderPage = orderRepository.findByUserIdWithDetails(userId, pageable);
    
    List<PurchaseHistoryItemResponse> historyItems = new ArrayList<>();
    for (Order order : orderPage.getContent()) {
      // Get order items for this order
      List<OrderItem> orderItems = orderItemRepository.findByOrderIdWithProduct(order.getId());
      
      // Map order items to DTOs
      List<OrderItemDetailResponse> itemResponses = orderItems.stream()
          .map(item -> OrderItemDetailResponse.builder()
              .productId(item.getProduct().getId())
              .productName(item.getProduct().getName())
              .quantity(item.getQuantity())
              .purchasedAtPrice(item.getPurchasedAtPrice())
              .brandName(item.getProduct().getBrand() != null ? item.getProduct().getBrand().getName() : "N/A")
              .thumbnailUrl(item.getProduct().getImageUrls() != null && !item.getProduct().getImageUrls().isEmpty() 
                  ? item.getProduct().getImageUrls().get(0) 
                  : null)
              .build())
          .toList();
      
      // Build purchase history item response
      PurchaseHistoryItemResponse historyItem = PurchaseHistoryItemResponse.builder()
          .orderId(order.getId())
          .orderDate(order.getCreatedAt())
          .status(order.getStatus().name())
          .totalAmount(order.getTotalAmount())
          .discountAmount(order.getDiscountAmount())
          .paymentMethod(order.getPaymentMethod())
          .trackingNumber(order.getTrackingNumber())
          .items(itemResponses)
          .build();
      
      historyItems.add(historyItem);
    }
    
    // Build paginated response
    return PurchaseHistoryResponse.builder()
        .content(historyItems)
        .currentPage(orderPage.getNumber())
        .totalPages(orderPage.getTotalPages())
        .totalElements(orderPage.getTotalElements())
        .pageSize(orderPage.getSize())
        .hasNext(orderPage.hasNext())
        .hasPrevious(orderPage.hasPrevious())
        .build();
  }
}
