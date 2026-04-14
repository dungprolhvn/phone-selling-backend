package ptit.ttcs.phone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "orderitem")
public class OrderItem {
  @EmbeddedId
  private OrderItemId id;
  
  @MapsId("orderId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "orderId", nullable = false)
  @JsonIgnore // avoid loop
  private Order order;
  
  @MapsId("productId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "productId", nullable = false)
  private Product product;
  
  @NotNull
  @ColumnDefault("1")
  @Column(name = "quantity", nullable = false)
  private Byte quantity;
  
  @NotNull
  @Column(name = "purchasedAtPrice", nullable = false, precision = 19)
  private BigDecimal purchasedAtPrice;
  
}