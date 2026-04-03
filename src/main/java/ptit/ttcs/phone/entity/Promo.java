package ptit.ttcs.phone.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ptit.ttcs.phone.enums.DiscountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "promo")
public class Promo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;
  
  @Size(max = 255)
  @NotNull
  @Column(name = "name", nullable = false)
  private String name;
  
  @Lob
  @Column(name = "description")
  private String description;
  
  @NotNull
  @Column(name = "startDate", nullable = false)
  private Instant startDate;
  
  @NotNull
  @Column(name = "endDate", nullable = false)
  private Instant endDate;
  
  @NotNull
  @Lob
  @Column(name = "discountType", nullable = false)
  private DiscountType discountType;
  
  @NotNull
  @Column(name = "discountValue", nullable = false, precision = 19)
  private BigDecimal discountValue;
  
  @Size(max = 50)
  @NotNull
  @Column(name = "voucherCode", nullable = false, length = 50)
  private String voucherCode;
  
  @NotNull
  @ColumnDefault("0")
  @Column(name = "usageLimit", nullable = false)
  private Integer usageLimit;
  
  @NotNull
  @ColumnDefault("0")
  @Column(name = "usageCount", nullable = false)
  private Integer usageCount;
  
  @Column(name = "maxDiscountMoneyPerOrder", precision = 19)
  private BigDecimal maxDiscountMoneyPerOrder;
  
  @Column(name = "promoImageUrls")
  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, Object> promoImageUrls;
  
  @NotNull
  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "createdAt", nullable = false)
  private Instant createdAt;
  
  @NotNull
  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "updatedAt", nullable = false)
  private Instant updatedAt;
  
}