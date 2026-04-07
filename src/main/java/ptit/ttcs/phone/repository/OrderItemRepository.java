package ptit.ttcs.phone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ptit.ttcs.phone.entity.OrderItem;
import ptit.ttcs.phone.entity.OrderItemId;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
  List<OrderItem> findByOrderId(Integer orderId);
}
