package ptit.ttcs.phone.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ptit.ttcs.phone.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "`order`")
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;
  
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "userId", nullable = false)
  private Account user;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "promoId")
  private Promo promo;
  
  @NotNull
  @ColumnDefault("0")
  @Column(name = "shipAtStore", nullable = false)
  private Boolean shipAtStore = false;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "shippingAddressId")
  private ShippingAddress shippingAddress;
  
  @Column(name = "paymentInitiatedAt")
  private Instant paymentInitiatedAt;
  
  @Size(max = 50)
  @Column(name = "paymentMethod", length = 50)
  private String paymentMethod;
  
  @NotNull
  @ColumnDefault("0")
  @Column(name = "totalAmount", nullable = false, precision = 19)
  private BigDecimal totalAmount;
  
  @NotNull
  @ColumnDefault("0")
  @Column(name = "discountAmount", nullable = false, precision = 19)
  private BigDecimal discountAmount = new BigDecimal(0.0);
  
  @Size(max = 100)
  @Column(name = "transactionId", length = 100)
  private String transactionId;
  
  @Size(max = 50)
  @Column(name = "trackingNumber", length = 50)
  private String trackingNumber;
  
  @Column(name = "cancelReason", columnDefinition = "TEXT")
  private String cancelReason;
  
  @NotNull
  @ColumnDefault("'PENDING_PAYMENT'")
  @Lob
  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private OrderStatus status;

  @CreationTimestamp
  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "createdAt", nullable = false)
  private Instant createdAt;
  
  @UpdateTimestamp
  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "updatedAt", nullable = false)
  private Instant updatedAt;
  
  @Column(name = "fulfilledAt")
  private Instant fulfilledAt;
  
}