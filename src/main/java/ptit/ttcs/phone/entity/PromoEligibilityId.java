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
public class PromoEligibilityId implements Serializable {
  private static final long serialVersionUID = 4306402597765532281L;
  @NotNull
  @Column(name = "promoId", nullable = false)
  private Integer promoId;
  
  @NotNull
  @Column(name = "productId", nullable = false)
  private Integer productId;
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    PromoEligibilityId entity = (PromoEligibilityId) o;
    return Objects.equals(this.productId, entity.productId) &&
        Objects.equals(this.promoId, entity.promoId);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(productId, promoId);
  }
  
}