package ptit.ttcs.phone.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class OrderItemId implements Serializable {
  private static final long serialVersionUID = 6578284825831786664L;
  @NotNull
  @Column(name = "orderId", nullable = false)
  private Integer orderId;
  
  @NotNull
  @Column(name = "productId", nullable = false)
  private Integer productId;
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    OrderItemId entity = (OrderItemId) o;
    return Objects.equals(this.productId, entity.productId) &&
        Objects.equals(this.orderId, entity.orderId);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(productId, orderId);
  }
  
}