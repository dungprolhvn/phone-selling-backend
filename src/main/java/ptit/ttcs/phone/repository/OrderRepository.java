package ptit.ttcs.phone.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ptit.ttcs.phone.entity.Order;
import ptit.ttcs.phone.entity.OrderItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
  // OrderRepository
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT o FROM Order o WHERE o.id = :id")
  Optional<Order> findByIdForUpdate(@Param("id") Integer id);
  
  // OrderItemRepository
  @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
  List<OrderItem> findByOrderId(@Param("orderId") Integer orderId);
  
  @Query(
      value = "SELECT * FROM `Order` WHERE status = 'PENDING_PAYMENT' AND paymentInitiatedAt < (NOW() - INTERVAL 15 MINUTE)",
      nativeQuery = true
  )
  List<Order> getUnpaidOrders();
}