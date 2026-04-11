package ptit.ttcs.phone.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ptit.ttcs.phone.dto.StatDTO;
import ptit.ttcs.phone.dto.StatRequest;
import ptit.ttcs.phone.exception.BadRequestException;
import ptit.ttcs.phone.repository.StatRepository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatService {

  private final StatRepository statRepository;

  @Transactional(readOnly = true)
  public StatDTO getStat(StatRequest request) {
    validateRequest(request);

    Instant startDate = request.getStartDate();
    Instant endDate = request.getEndDate();

    List<StatDTO.RevenueByDayItem> revenueByDay = statRepository.findRevenueByDay(startDate, endDate)
        .stream()
        .map(row -> StatDTO.RevenueByDayItem.builder()
            .day(toLocalDate(row[0]))
            .revenue(toBigDecimal(row[1]))
            .build())
        .toList();

    List<StatDTO.TotalRevenueItem> totalRevenue = List.of(
        StatDTO.TotalRevenueItem.builder()
            .totalRevenue(toBigDecimal(statRepository.getTotalRevenue(startDate, endDate)))
            .build()
    );

    List<StatDTO.TopPurchasedProductItem> topPurchasedProducts = statRepository.findTopPurchasedProducts(startDate, endDate)
        .stream()
        .map(row -> StatDTO.TopPurchasedProductItem.builder()
            .productId(toInt(row[0]))
            .productName((String) row[1])
            .totalPurchased(toLong(row[2]))
            .build())
        .toList();

    List<StatDTO.TopPaidOrderItem> topPaidOrders = statRepository.findTopPaidOrders(startDate, endDate)
        .stream()
        .map(row -> StatDTO.TopPaidOrderItem.builder()
            .orderId(toInt(row[0]))
            .paidAmount(toBigDecimal(row[1]))
            .status((String) row[2])
            .createdAt(toInstant(row[3]))
            .build())
        .toList();

    List<Object[]> countRows = statRepository.getOrderStatusCounts(startDate, endDate);
    Object[] countRow = countRows.isEmpty() ? null : countRows.get(0);
    if (countRow != null && countRow.length == 1 && countRow[0] instanceof Object[] nestedRow) {
      countRow = nestedRow;
    }

    long totalOrders = toLong(getRowValue(countRow, 0));
    long successOrders = toLong(getRowValue(countRow, 1));
    long cancelledOrders = toLong(getRowValue(countRow, 2));

    List<StatDTO.OrderRateItem> orderRates = List.of(
        StatDTO.OrderRateItem.builder()
            .status("SUCCESS")
            .count(successOrders)
            .rate(totalOrders == 0 ? 0.0 : (double) successOrders / totalOrders)
            .build(),
        StatDTO.OrderRateItem.builder()
            .status("CANCELLED")
            .count(cancelledOrders)
            .rate(totalOrders == 0 ? 0.0 : (double) cancelledOrders / totalOrders)
            .build()
    );

    Instant releaseDateThreshold = ZonedDateTime.now(ZoneOffset.UTC).minusMonths(1).toInstant();
    List<StatDTO.LowSellingNewProductItem> lowSellingNewProducts = statRepository.findLowSellingNewProducts(releaseDateThreshold)
        .stream()
        .map(row -> StatDTO.LowSellingNewProductItem.builder()
            .productId(toInt(row[0]))
            .productName((String) row[1])
            .releaseDate(toInstant(row[2]))
            .soldQuantity(toLong(row[3]))
            .build())
        .toList();

    return StatDTO.builder()
        .revenueByDay(revenueByDay)
        .totalRevenue(totalRevenue)
        .topPurchasedProducts(topPurchasedProducts)
        .topPaidOrders(topPaidOrders)
        .orderRates(orderRates)
        .lowSellingNewProducts(lowSellingNewProducts)
        .build();
  }

  private void validateRequest(StatRequest request) {
    if (request.getStartDate() == null || request.getEndDate() == null) {
      throw new BadRequestException("startDate and endDate are required");
    }
    if (request.getStartDate().isAfter(request.getEndDate())) {
      throw new BadRequestException("startDate must be less than or equal to endDate");
    }
  }

  private LocalDate toLocalDate(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof LocalDate localDate) {
      return localDate;
    }
    if (value instanceof Date date) {
      return date.toLocalDate();
    }
    if (value instanceof Timestamp timestamp) {
      return timestamp.toLocalDateTime().toLocalDate();
    }
    return LocalDate.parse(value.toString());
  }

  private Instant toInstant(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Instant instant) {
      return instant;
    }
    if (value instanceof Timestamp timestamp) {
      return timestamp.toInstant();
    }
    if (value instanceof Date date) {
      return date.toInstant();
    }
    if (value instanceof LocalDateTime localDateTime) {
      return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    String text = value.toString().trim();
    try {
      return Instant.parse(text);
    } catch (RuntimeException ignored) {
      // Continue to parse local datetime values returned by native queries.
    }

    try {
      return LocalDateTime.parse(text).atZone(ZoneId.systemDefault()).toInstant();
    } catch (RuntimeException ignored) {
      // Continue to parse common SQL datetime representation.
    }

    return LocalDateTime.parse(text.replace(' ', 'T'))
        .atZone(ZoneId.systemDefault())
        .toInstant();
  }

  private Integer toInt(Object value) {
    if (value == null) {
      return 0;
    }
    if (value instanceof Object[] arrayValue) {
      return arrayValue.length == 0 ? 0 : toInt(arrayValue[0]);
    }
    if (value instanceof Number number) {
      return number.intValue();
    }
    return Integer.parseInt(value.toString());
  }

  private Long toLong(Object value) {
    if (value == null) {
      return 0L;
    }
    if (value instanceof Object[] arrayValue) {
      return arrayValue.length == 0 ? 0L : toLong(arrayValue[0]);
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    return Long.parseLong(value.toString());
  }

  private Object getRowValue(Object[] row, int index) {
    if (row == null || index < 0 || index >= row.length) {
      return null;
    }
    return row[index];
  }

  private BigDecimal toBigDecimal(Object value) {
    if (value == null) {
      return BigDecimal.ZERO;
    }
    if (value instanceof BigDecimal bigDecimal) {
      return bigDecimal;
    }
    if (value instanceof Number number) {
      return new BigDecimal(number.toString());
    }
    return new BigDecimal(value.toString());
  }
}
