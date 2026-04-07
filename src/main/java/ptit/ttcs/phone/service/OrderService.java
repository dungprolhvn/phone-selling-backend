package ptit.ttcs.phone.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ptit.ttcs.phone.dto.Cart;
import ptit.ttcs.phone.dto.OrderRequest;
import ptit.ttcs.phone.dto.OrderResponse;
import ptit.ttcs.phone.entity.*;
import ptit.ttcs.phone.enums.DiscountType;
import ptit.ttcs.phone.enums.OrderStatus;
import ptit.ttcs.phone.enums.RefundStatus;
import ptit.ttcs.phone.exception.BadRequestException;
import ptit.ttcs.phone.exception.ConflictException;
import ptit.ttcs.phone.exception.NotFoundException;
import ptit.ttcs.phone.repository.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {
  private final VNPayService vnPayService;
  private final OrderRepository orderRepository;
  private final AccountRepository userRepository;
  private final ShippingAddressRepository shippingAddressRepository;
  private final PromoRepository promoRepository;
  private final ProductRepository productRepository;
  private final RefundRepository refundRepository;
  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;
  private final OrderItemRepository orderItemRepository;
  
  public OrderResponse processOrder(Authentication authentication, OrderRequest request, String clientIp) {
    int userId = (int) authentication.getPrincipal();
    Account user = userRepository.getReferenceById(userId);
    // tao don hang
    Order order = new Order();
    order.setUser(user);
    if (!request.getShipAtStore()) {
      // neu nhan hang ship ve nha
      ShippingAddress address = shippingAddressRepository
          .findById(request.getShippingAddressId())
          .orElseThrow(() -> new BadRequestException("Khong tim thay dia chi giao hang trong CSDL"));
      order.setShipAtStore(false);
      order.setShippingAddress(address);
    }
    // lay gio hang tu redis
    Cart userCart;
    try {
      userCart = objectMapper.readValue(
          redisTemplate.opsForValue().get(String.format("cart:user:%s", userId)),
          Cart.class
      );
    }
    catch (JsonProcessingException e) {
      throw new BadRequestException("Gio hang khong ton tai/khong co san pham");
    }
    
    // sap xep gio hang
    TreeMap<Integer, Integer> cartProducts = userCart.getSortedProducts();
    
    // tinh toan tong so tien
    HashMap<Product, Pair<Double, Integer>> paidValues = new HashMap<>();
    final double[] totalAmount = {0.0};
    cartProducts.forEach((productId, count) -> {
      Product p = productRepository.findById(productId)
          .orElseThrow(() -> new BadRequestException("1 san pham trong gio hang khong ton tai"));
      paidValues.put(p, Pair.of(p.getBasePrice().doubleValue(), count));
      totalAmount[0] += paidValues.get(p).getLeft() * paidValues.get(p).getRight();
    });
    order.setTotalAmount(new BigDecimal(totalAmount[0]));
    
    // tinh toan giam gia promo
    // nguyen tac ap dung promo:
    // 1 order = 1 usage count
    // usageCount co the > usageLimit neu con 1 usageCount, 2 order cung su dung (chap nhan duoc)
    // maxDiscountMoneyPerOrder: so tien giam gia VND toi da cho 1 don hang
    // kiem tra promo hop le
    HashMap<Product, Double> discountValues = null;
    if (request.getPromoCode() != null) {
      Promo promo;
      try {
        promo = promoRepository
            .getUsablePromoByVoucherCode(request.getPromoCode(), Instant.now())
            .orElseThrow(() -> new BadRequestException("ma giam gia khong hop le"));
      }
      catch (Exception e) {
        log.error(e.getMessage());
        throw new BadRequestException("ma giam gia khong hop le");
      }
      // kiem tra san pham nao trong gio ap dung duoc
      discountValues = new HashMap<>();
      final double[] totalDiscountedAmount = {0.0};
      double maxPossibleDiscountedAmount = promo.getMaxDiscountMoneyPerOrder().doubleValue();
      HashMap<Product, Double> finalDiscountValues = discountValues;
      paidValues.forEach((p, priceCountPair) -> {
        boolean isEligible = promoRepository.isEligibleForPromo(p.getId(), promo.getId());
        if (!isEligible) {
          return;
        }
        
        double discountValue = promo.getDiscountType() == DiscountType.FIXED ?
            p.getBasePrice().doubleValue() - promo.getDiscountValue().doubleValue() :
            p.getBasePrice().doubleValue() * promo.getDiscountValue().doubleValue() / 100;

        if (Double.compare(totalDiscountedAmount[0] + discountValue * priceCountPair.getRight(), maxPossibleDiscountedAmount) <= 0) {
          finalDiscountValues.put(p, discountValue);
          totalDiscountedAmount[0] += discountValue;
        }
      });
      if (Double.compare(totalDiscountedAmount[0], 0.0) > 0) {
        order.setDiscountAmount(new BigDecimal(totalDiscountedAmount[0]));
        order.setPromo(promo);
      }
    }
    // tao OrderItem list de luu vao csdl
    List<OrderItem> items = paidValues.entrySet().stream().map(entry -> {
      Product product = entry.getKey();
      Pair<Double, Integer> priceCountPair = entry.getValue();
      
      OrderItem item = new OrderItem();
      OrderItemId itemId = new OrderItemId();
      itemId.setOrderId(order.getId());
      itemId.setProductId(product.getId());
      
      item.setId(itemId);
      item.setOrder(order);
      item.setProduct(product);
      item.setQuantity(priceCountPair.getRight().byteValue());
      item.setPurchasedAtPrice(BigDecimal.valueOf(priceCountPair.getLeft()));
      
      return item;
    }).toList();
    // bat dau giao dich
    String paymentUrl = placeOrder(order, items, discountValues, paidValues, request.getPaymentMethod(), clientIp);
    // ket thuc giao dich
    // xoa gio hang khoi redis
    return new OrderResponse(
        order.getId(),
        paymentUrl,
        order.getStatus());
  }
  
  @Transactional
  protected String placeOrder(
      Order order,
      List<OrderItem> items,
      Map<Product, Double> discountValues,
      Map<Product, Pair<Double, Integer>> paidValues,
      int paymentMethod,
      String clientIp) {
    for (OrderItem item : items) {
      updateDbStock(item.getProduct().getId(), item.getQuantity());
    }
    String paymentUrl = null;
    // tuong tac payment gateway
    if (paymentMethod == 0) {
      order.setStatus(OrderStatus.PENDING_PAYMENT);
      paymentUrl = vnPayService.createPaymentUrl(
          order.getId(),
          order.getTotalAmount().subtract(
              order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO
          ),
          "Thanh toan don hang #" + order.getId(),
          clientIp
      );
      order.setPaymentInitiatedAt(Instant.now());
    }
    else {
      order.setStatus(OrderStatus.PENDING);
    }
    // luu thong tin don hang vao sql
    orderRepository.save(order);
    orderItemRepository.saveAll(items);
    return paymentUrl;
  }
  
  @Transactional
  protected void updateDbStock(int productId, int quantity) {
    Product p = productRepository.getProductByIdForUpdate(productId)
        .orElseThrow(() -> new BadRequestException("Khong tim thay san pham"));
    if (p.getStockAvailable() < quantity) {
      log.debug("XUNG DOT: San pham {}, id {} het hang trong khi dang duoc dat", p.getName(), p.getId());
      throw new ConflictException(String.format("San pham %s, id %s da het hang. Xin quy khach thong cam", p.getName(), p.getId()));
    }
    p.setStockReserved(p.getStockReserved() + quantity);
    p.setStockAvailable(p.getStockAvailable() - quantity);
  }
  
  @Transactional
  public void confirmPayment(Integer orderId, String transactionId) {
    // 1. Fetch order with lock — prevents scheduler from cancelling
    //    the same order simultaneously
    Order order = orderRepository.findByIdForUpdate(orderId)
        .orElseThrow(() -> new NotFoundException("Khong tim thay don hang: " + orderId));
    
    // 2. Handle each possible status
    switch (order.getStatus()) {
      
      case CONFIRMED -> {
        // Duplicate webhook — payment already processed
        // Return silently — do not process again
        log.info("Webhook trung lap cho don hang {}, bo qua", orderId);
      }
      
      case PENDING_PAYMENT -> {
        // Normal flow — confirm the order
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTransactionId(transactionId);
        order.setFulfilledAt(Instant.now());
        
        // Release stock_reserved — product is now permanently sold
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        items.forEach(item ->
            releaseReservedStock(item.getProduct().getId(), item.getQuantity())
        );
        
        orderRepository.save(order);
        log.info("Xac nhan thanh toan thanh cong cho don hang {}", orderId);
      }
      
      case CANCELLED -> {
        // Race condition — order timed out but user still paid
        // Must refund immediately
        log.warn("Don hang {} da bi huy nhung van nhan duoc thanh toan, tien hanh hoan tien", orderId);
        order.setTransactionId(transactionId);
        orderRepository.save(order);
        initiateRefund(order, transactionId);
      }
      
      default -> {
        // PENDING, DELIVERYING, SUCCESS — unexpected status for a payment webhook
        log.error("Trang thai khong hop le {} cho don hang {} khi xu ly webhook", order.getStatus(), orderId);
      }
    }
  }
  
  @Transactional
  protected void releaseReservedStock(int productId, int quantity) {
    Product p = productRepository.getProductByIdForUpdate(productId)
        .orElseThrow(() -> new NotFoundException("Khong tim thay san pham: " + productId));
    p.setStockReserved(p.getStockReserved() - quantity);
    productRepository.save(p);
  }
  
  private void initiateRefund(Order order, String transactionId) {
    // Save refund record — status PENDING
    Refund refund = new Refund();
    refund.setOrder(order);
    refund.setTransactionId(transactionId);
    refund.setAmount(
        order.getTotalAmount().subtract(
            order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO
        )
    );
    refund.setStatus(RefundStatus.PENDING);
    refund.setRequestedAt(Instant.now());
    refundRepository.save(refund);
    
    // Attempt VNPay refund API call
    // VNPay refund is a separate API call — implement when needed
    // For now log and flag for manual admin review
    log.error("CAN XU LY THU CONG: Hoan tien cho don hang {}, giao dich {}, so tien {}",
        order.getId(), transactionId, refund.getAmount());
    
    // TODO: call VNPayService.refund(transactionId, amount) when implemented
  }
  
  @Scheduled(cron = "0 * * * * *")
  @Transactional
  public void cleanTimedoutOrders() {
    List<Order> orders = orderRepository.getUnpaidOrders();
    for (Order o : orders) {
      cleanTimedoutOrder(o);
    }
  }
  
  @Transactional
  protected void cleanTimedoutOrder(Order o) {
    List<OrderItem> items = orderItemRepository.findByOrderId(o.getId());
    for (OrderItem item : items) {
      int productId = item.getProduct().getId();
      updateDbStock(productId, -item.getQuantity());
    }
    o.setStatus(OrderStatus.CANCELLED);
    orderRepository.save(o);
  }
}
