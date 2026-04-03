package ptit.ttcs.phone.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "cartfallback")
public class CartFallback {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;
  
  @Size(max = 100)
  @NotNull
  @Column(name = "cartKey", nullable = false, length = 100)
  private String cartKey;
  
  @NotNull
  @Column(name = "cartData", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, Object> cartData;
  
  @NotNull
  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "savedAt", nullable = false)
  private Instant savedAt;
  
  @NotNull
  @ColumnDefault("0")
  @Column(name = "restored", nullable = false)
  private Boolean restored = false;
  
}