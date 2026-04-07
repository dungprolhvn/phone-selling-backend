package ptit.ttcs.phone.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import ptit.ttcs.phone.entity.Order;
import ptit.ttcs.phone.entity.OrderItem;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
  // OrderRepository
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT o FROM Order o WHERE o.id = :id")
  Optional<Order> findByIdForUpdate(@Param("id") Integer id);

  @Query("SELECT o FROM Order o JOIN FETCH o.user LEFT JOIN FETCH o.shippingAddress WHERE o.id = :orderId AND o.user.phone = :phone")
  Optional<Order> findByIdAndUserPhone(@Param("orderId") Integer orderId, @Param("phone") String phone);

  @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.product WHERE oi.order.id = :orderId")
  List<OrderItem> findByOrderIdWithProduct(@Param("orderId") Integer orderId);
  
  @Query(
      value = "SELECT * FROM `Order` WHERE status = 'PENDING_PAYMENT' AND paymentInitiatedAt < (NOW() - INTERVAL 15 MINUTE)",
      nativeQuery = true
  )
  List<Order> getUnpaidOrders();
}