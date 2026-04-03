package ptit.ttcs.phone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "promoeligibility")
public class PromoEligibility {
  @EmbeddedId
  private PromoEligibilityId id;
  
  @MapsId("promoId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "promoId", nullable = false)
  private Promo promo;
  
  @MapsId("productId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "productId", nullable = false)
  private Product product;
  
}