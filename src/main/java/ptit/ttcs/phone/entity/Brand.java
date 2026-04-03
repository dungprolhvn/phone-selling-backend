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
@Table(name = "brand")
public class Brand {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;
  
  @Size(max = 255)
  @NotNull
  @Column(name = "name", nullable = false)
  private String name;
  
  @Column(name = "logoImageUrls")
  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, Object> logoImageUrls;
  
  @NotNull
  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "createdAt", nullable = false)
  private Instant createdAt;
  
}