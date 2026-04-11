package ptit.ttcs.phone.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import ptit.ttcs.phone.entity.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@org.springframework.stereotype.Repository
public interface StatRepository extends Repository<Order, Integer> {

  @Query(
      value = "SELECT DATE(o.createdAt) AS day, COALESCE(SUM(o.totalAmount - o.discountAmount), 0) AS revenue " +
          "FROM `order` o " +
          "WHERE o.status = 'SUCCESS' " +
          "AND o.createdAt >= :startDate " +
          "AND o.createdAt <= :endDate " +
          "GROUP BY DATE(o.createdAt) " +
          "ORDER BY day ASC",
      nativeQuery = true
  )
  List<Object[]> findRevenueByDay(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  @Query(
      value = "SELECT COALESCE(SUM(o.totalAmount - o.discountAmount), 0) " +
          "FROM `order` o " +
          "WHERE o.status = 'SUCCESS' " +
          "AND o.createdAt >= :startDate " +
          "AND o.createdAt <= :endDate",
      nativeQuery = true
  )
  BigDecimal getTotalRevenue(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  @Query(
      value = "SELECT p.id, p.name, COALESCE(SUM(oi.quantity), 0) AS totalPurchased " +
          "FROM orderitem oi " +
          "JOIN `order` o ON o.id = oi.orderId " +
          "JOIN product p ON p.id = oi.productId " +
          "WHERE o.createdAt >= :startDate " +
          "AND o.createdAt <= :endDate " +
          "AND o.status = 'SUCCESS' " +
          "GROUP BY p.id, p.name " +
          "ORDER BY totalPurchased DESC " +
          "LIMIT 5",
      nativeQuery = true
  )
  List<Object[]> findTopPurchasedProducts(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  @Query(
      value = "SELECT o.id, (o.totalAmount - o.discountAmount) AS paidAmount, o.status, o.createdAt " +
          "FROM `order` o " +
          "WHERE o.createdAt >= :startDate " +
          "AND o.createdAt <= :endDate " +
          "ORDER BY paidAmount DESC " +
          "LIMIT 5",
      nativeQuery = true
  )
  List<Object[]> findTopPaidOrders(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  @Query(
      value = "SELECT " +
          "COUNT(*) AS totalOrders, " +
          "COALESCE(SUM(CASE WHEN o.status = 'SUCCESS' THEN 1 ELSE 0 END), 0) AS successOrders, " +
          "COALESCE(SUM(CASE WHEN o.status = 'CANCELLED' THEN 1 ELSE 0 END), 0) AS cancelledOrders " +
          "FROM `order` o " +
          "WHERE o.createdAt >= :startDate " +
          "AND o.createdAt <= :endDate",
      nativeQuery = true
  )
  List<Object[]> getOrderStatusCounts(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  @Query(
      value = "SELECT " +
          "p.id, " +
          "p.name, " +
          "p.releaseDate, " +
          "COALESCE(SUM(CASE WHEN o.status = 'SUCCESS' THEN oi.quantity ELSE 0 END), 0) AS soldQuantity " +
          "FROM product p " +
          "LEFT JOIN orderitem oi ON oi.productId = p.id " +
          "LEFT JOIN `order` o ON o.id = oi.orderId " +
          "WHERE p.releaseDate IS NOT NULL " +
          "AND p.releaseDate <= :releaseDateThreshold " +
          "GROUP BY p.id, p.name, p.releaseDate " +
          "HAVING COALESCE(SUM(CASE WHEN o.status = 'SUCCESS' THEN oi.quantity ELSE 0 END), 0) < 5 " +
          "ORDER BY soldQuantity ASC, p.releaseDate DESC",
      nativeQuery = true
  )
  List<Object[]> findLowSellingNewProducts(@Param("releaseDateThreshold") Instant releaseDateThreshold);
}
