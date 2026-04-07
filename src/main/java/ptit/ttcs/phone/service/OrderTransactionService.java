package ptit.ttcs.phone.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ptit.ttcs.phone.entity.Order;
import ptit.ttcs.phone.entity.OrderItem;
import ptit.ttcs.phone.entity.Product;
import ptit.ttcs.phone.entity.Refund;
import ptit.ttcs.phone.enums.OrderStatus;
import ptit.ttcs.phone.enums.RefundStatus;
import ptit.ttcs.phone.exception.ConflictException;
import ptit.ttcs.phone.exception.NotFoundException;
import ptit.ttcs.phone.repository.OrderItemRepository;
import ptit.ttcs.phone.repository.OrderRepository;
import ptit.ttcs.phone.repository.ProductRepository;
import ptit.ttcs.phone.repository.RefundRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderTransactionService {
  
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final ProductRepository productRepository;
  private final RefundRepository refundRepository;
  
  @Transactional
  public Order saveOrderWithStockReservation(Order order, List<OrderItem> items) {
    for (OrderItem item : items) {
      deductStock(item.getProduct().getId(), item.getQuantity());
    }
    
    // save order to get id
    Order savedOrder = orderRepository.save(order);
    // save order item
    items.forEach(item -> {
      item.getId().setOrderId(savedOrder.getId());
      item.setOrder(savedOrder);
    });
    orderItemRepository.saveAll(items);
    
    return savedOrder;
  }
  
  @Transactional
  public void confirmPayment(Integer orderId, String transactionId) {
    Order order = orderRepository.findByIdForUpdate(orderId)
        .orElseThrow(() -> new NotFoundException("Khong tim thay don hang: " + orderId));
    
    switch (order.getStatus()) {
      
      case CONFIRMED -> {
        log.info("Webhook trung lap cho don hang {}, bo qua", orderId);
      }
      
      case PENDING_PAYMENT -> {
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTransactionId(transactionId);
        order.setFulfilledAt(Instant.now());
        
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        items.forEach(item ->
            releaseReservedStock(item.getProduct().getId(), item.getQuantity())
        );
        
        orderRepository.save(order);
        log.info("Xac nhan thanh toan thanh cong cho don hang {}", orderId);
      }
      
      case CANCELLED -> {
        log.warn("Don hang {} da bi huy nhung van nhan duoc thanh toan, tien hanh hoan tien", orderId);
        order.setTransactionId(transactionId);
        orderRepository.save(order);
        initiateRefund(order, transactionId);
      }
      
      default -> {
        log.error("Trang thai khong hop le {} cho don hang {} khi xu ly webhook",
            order.getStatus(), orderId);
      }
    }
  }

  
  @Scheduled(cron = "0 * * * * *")
  @Transactional
  public void cleanTimedoutOrders() {
    List<Order> orders = orderRepository.getUnpaidOrders();
    for (Order order : orders) {
      cleanSingleTimedoutOrder(order);
    }
  }
  
  // ── HELPERS ───────────────────────────────────────────────────
  
  private void cleanSingleTimedoutOrder(Order order) {
    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
    for (OrderItem item : items) {
      deductStock(item.getProduct().getId(), -item.getQuantity());
    }
    order.setStatus(OrderStatus.CANCELLED);
    orderRepository.save(order);
    log.info("Don hang {} da bi huy do het han thanh toan", order.getId());
  }
  
  private void deductStock(int productId, int quantity) {
    Product p = productRepository.getProductByIdForUpdate(productId)
        .orElseThrow(() -> new NotFoundException("Khong tim thay san pham: " + productId));
    
    int newAvailable = p.getStockAvailable() - quantity;
    int newReserved = p.getStockReserved() + quantity;
    
    if (newAvailable < 0) {
      log.debug("XUNG DOT: San pham {}, id {} het hang", p.getName(), p.getId());
      throw new ConflictException(String.format(
          "San pham %s da het hang. Xin quy khach thong cam", p.getName()
      ));
    }
    
    p.setStockAvailable(newAvailable);
    p.setStockReserved(newReserved);
    productRepository.save(p);
  }
  
  private void releaseReservedStock(int productId, int quantity) {
    Product p = productRepository.getProductByIdForUpdate(productId)
        .orElseThrow(() -> new NotFoundException("Khong tim thay san pham: " + productId));
    p.setStockReserved(p.getStockReserved() - quantity);
    productRepository.save(p);
  }
  
  private void initiateRefund(Order order, String transactionId) {
    Refund refund = new Refund();
    refund.setOrder(order);
    refund.setTransactionId(transactionId);
    refund.setAmount(
        order.getTotalAmount().subtract(
            order.getDiscountAmount() != null
                ? order.getDiscountAmount()
                : BigDecimal.ZERO
        )
    );
    refund.setStatus(RefundStatus.PENDING);
    refund.setRequestedAt(Instant.now());
    refundRepository.save(refund);
    
    log.error("CAN XU LY THU CONG: Hoan tien cho don hang {}, giao dich {}, so tien {}",
        order.getId(), transactionId, refund.getAmount());
  }
}
