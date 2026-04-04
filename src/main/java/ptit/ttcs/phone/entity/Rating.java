package ptit.ttcs.phone.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "rating")
public class Rating {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;
  
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "userId", nullable = false)
  private Account user;
  
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "productId", nullable = false)
  private Product product;
  
  @NotNull
  @Column(name = "star", nullable = false)
  private Byte star;
  
  @Column(name = "comment", columnDefinition = "TEXT")
  private String comment;
  
  @NotNull
  @ColumnDefault("0")
  @Column(name = "hidden", nullable = false)
  private Boolean hidden = false;
  
  @Column(name = "hideReason", columnDefinition = "TEXT")
  private String hideReason;
  
  @NotNull
  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "createdAt", nullable = false)
  private Instant createdAt;
  
  @NotNull
  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "updatedAt", nullable = false)
  private Instant updatedAt;
  
}