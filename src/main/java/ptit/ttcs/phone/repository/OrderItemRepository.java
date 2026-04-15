package ptit.ttcs.phone.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ptit.ttcs.phone.entity.OrderItem;
import ptit.ttcs.phone.entity.OrderItemId;
import ptit.ttcs.phone.enums.OrderStatus;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
  List<OrderItem> findByOrderId(Integer orderId);

  @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.product p JOIN FETCH p.brand WHERE oi.order.id = :orderId")
  List<OrderItem> findByOrderIdWithProduct(@Param("orderId") Integer orderId);

  default boolean checkUserPurchasedProduct(Integer userId, Integer productId) {
    return checkUserPurchasedProduct(userId, productId, List.of(OrderStatus.CONFIRMED, OrderStatus.SUCCESS));
  }

  @Query(
      "SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END " +
      "FROM OrderItem oi " +
      "JOIN oi.order o " +
      "WHERE o.user.id = :userId " +
      "AND oi.product.id = :productId " +
      "AND o.status IN :statuses"
  )
  boolean checkUserPurchasedProduct(
      @Param("userId") Integer userId,
      @Param("productId") Integer productId,
      @Param("statuses") Collection<OrderStatus> statuses);
}
