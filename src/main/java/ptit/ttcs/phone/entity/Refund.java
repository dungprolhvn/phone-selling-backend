package ptit.ttcs.phone.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import ptit.ttcs.phone.enums.RefundStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "refund")
public class Refund {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;
  
  @NotNull
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "orderId", nullable = false)
  private Order order;
  
  @Size(max = 100)
  @NotNull
  @Column(name = "transactionId", nullable = false, length = 100)
  private String transactionId;
  
  @NotNull
  @Column(name = "amount", nullable = false, precision = 19)
  private BigDecimal amount;
  
  @NotNull
  @ColumnDefault("'PENDING'")
  @Lob
  @Column(name = "status", nullable = false)
  private RefundStatus status;
  
  @NotNull
  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "requestedAt", nullable = false)
  private Instant requestedAt;
  
  @Column(name = "completedAt")
  private Instant completedAt;
  
}