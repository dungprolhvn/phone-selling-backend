package ptit.ttcs.phone.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ptit.ttcs.phone.enums.ProductType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "product")
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;
  
  @NotNull
  @Lob
  @Column(name = "type", nullable = false)
  @Enumerated(EnumType.STRING)
  private ProductType type;
  
  @Size(max = 255)
  @NotNull
  @Column(name = "name", nullable = false)
  private String name;
  
  @NotNull
  @Column(name = "basePrice", nullable = false, precision = 19)
  private BigDecimal basePrice;
  
  @Column(name = "specs")
  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, Object> specs;
  
  @Column(name = "description", columnDefinition = "TEXT")
  private String description;
  
  @Column(name = "releaseDate")
  private Instant releaseDate;
  
  @Column(name = "imageUrls")
  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, Object> imageUrls;
  
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "brandId", nullable = false)
  private Brand brand;
  
  @NotNull
  @ColumnDefault("0")
  @Column(name = "stockAvailable", nullable = false)
  private Integer stockAvailable;
  
  @NotNull
  @ColumnDefault("0")
  @Column(name = "stockReserved", nullable = false)
  private Integer stockReserved;
  
  @NotNull
  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "createdAt", nullable = false)
  private Instant createdAt;
  
  @NotNull
  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "updatedAt", nullable = false)
  private Instant updatedAt;
  
}