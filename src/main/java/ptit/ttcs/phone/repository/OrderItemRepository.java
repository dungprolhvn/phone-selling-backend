package ptit.ttcs.phone.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ptit.ttcs.phone.entity.OrderItem;
import ptit.ttcs.phone.entity.OrderItemId;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
  List<OrderItem> findByOrderId(Integer orderId);

  @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.product WHERE oi.order.id = :orderId")
  List<OrderItem> findByOrderIdWithProduct(@Param("orderId") Integer orderId);
}
