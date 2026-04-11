package ptit.ttcs.phone.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ptit.ttcs.phone.dto.*;
import ptit.ttcs.phone.entity.*;
import ptit.ttcs.phone.enums.DiscountType;
import ptit.ttcs.phone.enums.OrderStatus;
import ptit.ttcs.phone.enums.RefundStatus;
import ptit.ttcs.phone.exception.BadRequestException;
import ptit.ttcs.phone.exception.ConflictException;
import ptit.ttcs.phone.exception.ForbiddenException;
import ptit.ttcs.phone.exception.NotFoundException;
import ptit.ttcs.phone.repository.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;


@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {
  
  private final VNPayService vnPayService;
  private final OrderTransactionService orderTransactionService;
  private final AccountRepository userRepository;
  private final ShippingAddressRepository shippingAddressRepository;
  private final PromoRepository promoRepository;
  private final ProductRepository productRepository;
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final RefundRepository refundRepository;
  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;
  
  // No @Transactional — intentionally split into two phases
  public OrderResponse processOrder(
      Authentication authentication,
      OrderRequest request,
      String clientIp) {
    
    int userId = (int) authentication.getPrincipal();
    Account user = userRepository.getReferenceById(userId);
    
    // Build order object
    Order order = new Order();
    order.setUser(user);
    
    if (!request.getShipAtStore()) {
      ShippingAddress address = shippingAddressRepository
          .findById(request.getShippingAddressId())
          .orElseThrow(() -> new BadRequestException("Khong tim thay dia chi giao hang trong CSDL"));
      order.setShipAtStore(false);
      order.setShippingAddress(address);
    } else {
      order.setShipAtStore(true);
    }
    
    // Fetch cart from Redis
    Cart userCart;
    try {
      String cartJson = redisTemplate.opsForValue().get(
          String.format("cart:user:%s", userId)
      );
      userCart = objectMapper.readValue(cartJson, Cart.class);
    } catch (JsonProcessingException e) {
      throw new BadRequestException("Gio hang khong ton tai/khong co san pham");
    }
    
    TreeMap<Integer, Integer> cartProducts = userCart.getSortedProducts();
    
    // Calculate total amount
    HashMap<Product, Pair<Double, Integer>> paidValues = new HashMap<>();
    final double[] totalAmount = {0.0};
    cartProducts.forEach((productId, count) -> {
      Product p = productRepository.findById(productId)
          .orElseThrow(() -> new BadRequestException("1 san pham trong gio hang khong ton tai"));
      paidValues.put(p, Pair.of(p.getBasePrice().doubleValue(), count));
      totalAmount[0] += p.getBasePrice().doubleValue() * count;
    });
    order.setTotalAmount(new BigDecimal(totalAmount[0]));
    
    // Apply promo if provided
    if (request.getPromoCode() != null) {
      applyPromo(request.getPromoCode(), order, paidValues);
    }
    
    // Build OrderItems — orderId is null at this point
    List<OrderItem> items = paidValues.entrySet().stream().map(entry -> {
      Product product = entry.getKey();
      Pair<Double, Integer> priceCountPair = entry.getValue();
      
      OrderItem item = new OrderItem();
      OrderItemId itemId = new OrderItemId();
      itemId.setProductId(product.getId());
      // orderId set to 0 — will be updated inside saveOrderWithStockReservation
      itemId.setOrderId(0);
      
      item.setId(itemId);
      item.setOrder(order);
      item.setProduct(product);
      item.setQuantity(priceCountPair.getRight().byteValue());
      item.setPurchasedAtPrice(BigDecimal.valueOf(priceCountPair.getLeft()));
      
      return item;
    }).toList();
    
    if (request.getPaymentMethod() == 0) {
      order.setStatus(OrderStatus.PENDING_PAYMENT);
      order.setPaymentMethod("VNPAY");
      order.setPaymentInitiatedAt(Instant.now());
    }
    else {
      order.setStatus(OrderStatus.PENDING);
      order.setPaymentMethod("COD");
    }
    Order savedOrder = orderTransactionService.saveOrderWithStockReservation(order, items);
    
    String paymentUrl = null;
    if (request.getPaymentMethod() == 0) {
      paymentUrl = vnPayService.createPaymentUrl(
          savedOrder.getId(),
          savedOrder.getTotalAmount().subtract(
              savedOrder.getDiscountAmount() != null
                  ? savedOrder.getDiscountAmount()
                  : BigDecimal.ZERO
          ),
          "Thanh toan don hang #" + savedOrder.getId(),
          clientIp
      );
    }
    
    // Delete cart from Redis
    redisTemplate.delete(String.format("cart:user:%s", userId));
    
    return new OrderResponse(savedOrder.getId(), paymentUrl, savedOrder.getStatus());
  }
  
  @Transactional(readOnly = true)
  public PurchaseHistoryResponse getPurchaseHistory(Integer userId, Pageable pageable) {
    Page<Order> orderPage = orderRepository.findByUserIdWithDetails(userId, pageable);

    List<PurchaseHistoryItemResponse> historyItems = new ArrayList<>();
    for (Order order : orderPage.getContent()) {
      List<OrderItem> orderItems = orderItemRepository.findByOrderIdWithProduct(order.getId());

      List<OrderItemResponse> itemResponses = orderItems.stream()
          .map(item -> OrderItemResponse.builder()
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

        @Transactional(readOnly = true)
        public Order getOrderById(Integer orderId) {
          return orderRepository.findByIdWithDetails(orderId)
          .orElseThrow(() -> new NotFoundException("Khong tim thay don hang"));
        }

  @Transactional
  public OrderResponse cancelOrder(Integer userId, Integer orderId, String cancelReason) {
    Order order = orderRepository.findByIdForUpdate(orderId)
        .orElseThrow(() -> new NotFoundException("Khong tim thay don hang"));

    OrderStatus previousStatus = order.getStatus();

    if (!order.getUser().getId().equals(userId)) {
      throw new ForbiddenException("Ban khong co quyen huy don hang nay");
    }

    if (!isCancellableStatus(previousStatus)) {
      throw new BadRequestException("Don hang khong the huy o trang thai hien tai");
    }

    List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
    rollbackStockForCancellation(items, previousStatus);

    order.setStatus(OrderStatus.CANCELLED);
    order.setCancelReason(normalizeCancelReason(cancelReason));
    Order savedOrder = orderRepository.save(order);

    if (previousStatus == OrderStatus.CONFIRMED) {
      createPendingRefund(savedOrder);
    }

    log.info("User {} cancelled order {}", userId, orderId);
    return new OrderResponse(savedOrder.getId(), null, savedOrder.getStatus());
  }

  @Transactional
  public OrderResponse updateOrderStatusByWarehouse(WarehouseOrderStatusUpdateRequest request) {
    OrderStatus targetStatus = request.getStatus();
    Integer orderId = request.getOrderId();

    Order order = orderRepository.findByIdForUpdate(orderId)
        .orElseThrow(() -> new NotFoundException("Khong tim thay don hang"));

    OrderStatus currentStatus = order.getStatus();
    if (!isWarehouseTransitionAllowed(currentStatus, targetStatus)) {
      throw new BadRequestException(String.format(
          "Khong the cap nhat trang thai tu %s sang %s",
          currentStatus,
          targetStatus
      ));
    }

    order.setStatus(targetStatus);
    if (targetStatus == OrderStatus.CANCELLED) {
      order.setCancelReason(normalizeCancelReason(request.getCancelReason()));
      order.setFulfilledAt(Instant.now());
    }
    else if (targetStatus == OrderStatus.SUCCESS) {
      order.setFulfilledAt(Instant.now());
    }

    Order savedOrder = orderRepository.save(order);
    return new OrderResponse(savedOrder.getId(), null, savedOrder.getStatus());
  }

  // ── HELPERS ───────────────────────────────────────────────────

  private boolean isWarehouseTransitionAllowed(OrderStatus currentStatus, OrderStatus targetStatus) {
    if (currentStatus == OrderStatus.PENDING || currentStatus == OrderStatus.PENDING_PAYMENT) {
      return targetStatus == OrderStatus.DELIVERYING;
    }
    if (currentStatus == OrderStatus.DELIVERYING) {
      return targetStatus == OrderStatus.SUCCESS || targetStatus == OrderStatus.CANCELLED;
    }
    return false;
  }

  private boolean isCancellableStatus(OrderStatus status) {
    return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
  }

  private void rollbackStockForCancellation(List<OrderItem> items, OrderStatus previousStatus) {
    for (OrderItem item : items) {
      Product product = productRepository.getProductByIdForUpdate(item.getProduct().getId())
          .orElseThrow(() -> new NotFoundException("Khong tim thay san pham: " + item.getProduct().getId()));

      int quantity = item.getQuantity();
      if (previousStatus == OrderStatus.PENDING) {
        int newReserved = product.getStockReserved() - quantity;
        if (newReserved < 0) {
          throw new ConflictException("Du lieu ton kho khong hop le khi huy don");
        }
        product.setStockReserved(newReserved);
      }

      product.setStockAvailable(product.getStockAvailable() + quantity);
      productRepository.save(product);
    }
  }

  private void createPendingRefund(Order order) {
    Refund refund = new Refund();
    refund.setOrder(order);
    refund.setTransactionId(resolveRefundTransactionId(order));
    refund.setAmount(order.getTotalAmount().subtract(
        order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO
    ));
    refund.setStatus(RefundStatus.PENDING);
    refund.setRequestedAt(Instant.now());
    refundRepository.save(refund);

    log.warn("Order {} cancelled after payment; pending refund record created", order.getId());
  }

  private String resolveRefundTransactionId(Order order) {
    if (order.getTransactionId() != null && !order.getTransactionId().isBlank()) {
      return order.getTransactionId();
    }
    return "REFUND_ORDER_" + order.getId();
  }

  private String normalizeCancelReason(String cancelReason) {
    if (cancelReason == null) {
      return null;
    }
    String trimmed = cancelReason.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
  
  private void applyPromo(
      String promoCode,
      Order order,
      HashMap<Product, Pair<Double, Integer>> paidValues) {
    
    Promo promo;
    try {
      promo = promoRepository
          .getUsablePromoByVoucherCode(promoCode, Instant.now())
          .orElseThrow(() -> new BadRequestException("Ma giam gia khong hop le"));
    }
    catch (Exception e) {
      log.error(e.getMessage());
      throw new BadRequestException("Ma giam gia khong hop le");
    }
    
    final double[] totalDiscountedAmount = {0.0};
    double maxDiscount = promo.getMaxDiscountMoneyPerOrder().doubleValue();
    
    paidValues.forEach((p, priceCountPair) -> {
      boolean isEligible = promoRepository.isEligibleForPromo(p.getId(), promo.getId());
      if (!isEligible) return;
      
      double discountValue = promo.getDiscountType() == DiscountType.FIXED
          ? p.getBasePrice().doubleValue() - promo.getDiscountValue().doubleValue()
          : p.getBasePrice().doubleValue() * promo.getDiscountValue().doubleValue() / 100;
      
      if (Double.compare(
          totalDiscountedAmount[0] + discountValue * priceCountPair.getRight(),
          maxDiscount) <= 0) {
        totalDiscountedAmount[0] += discountValue * priceCountPair.getRight();
      }
    });
    
    if (Double.compare(totalDiscountedAmount[0], 0.0) > 0) {
      order.setDiscountAmount(new BigDecimal(totalDiscountedAmount[0]));
      order.setPromo(promo);
    }
  }
  
  public @Nullable List<Order> getUnconfirmedOrders() {
    return orderRepository.getUnconfirmedOrders();
  }
  
  @Transactional
  public void confirmOrder(Integer orderId) {
    Order order = orderRepository.findByIdForUpdate(orderId)
        .orElseThrow(() -> new NotFoundException("Khong tim thay don hang"));
    boolean isConfirmable = order.getStatus() == OrderStatus.CONFIRMED
        || order.getStatus() == OrderStatus.PENDING; // Da thanh toan hoac COD
    if (isConfirmable) {
      order.setStatus(OrderStatus.DELIVERYING);
      orderRepository.save(order);
    }
    else {
      throw new BadRequestException("Khong the xac nhan don hang vao luc nay");
    }
    return;
  }
}
