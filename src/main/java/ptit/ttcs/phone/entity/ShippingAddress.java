package ptit.ttcs.phone.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "shippingaddress")
public class ShippingAddress {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;
  
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "userId", nullable = false)
  private Account user;
  
  @Size(max = 255)
  @NotNull
  @Column(name = "recipientName", nullable = false)
  private String recipientName;
  
  @Size(max = 10)
  @NotNull
  @Column(name = "recipientPhone", nullable = false, length = 10)
  private String recipientPhone;
  
  @NotNull
  @Lob
  @Column(name = "address", nullable = false)
  private String address;
  
  @NotNull
  @ColumnDefault("0")
  @Column(name = "isDefault", nullable = false)
  private Boolean isDefault = false;
  
  @NotNull
  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "createdAt", nullable = false)
  private Instant createdAt;
  
}