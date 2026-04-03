package ptit.ttcs.phone.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import ptit.ttcs.phone.enums.AccountRole;
import ptit.ttcs.phone.enums.AccountStatus;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "account")
public class Account {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;
  
  @Size(max = 255)
  @NotNull
  @Column(name = "name", nullable = false)
  private String name;
  
  @Size(max = 255)
  @NotNull
  @Column(name = "email", nullable = false)
  private String email;
  
  @Size(max = 10)
  @Column(name = "phone", length = 10)
  private String phone;
  
  @Size(max = 255)
  @NotNull
  @Column(name = "passwordHash", nullable = false)
  private String passwordHash;
  
  @NotNull
  @ColumnDefault("'USER'")
  @Lob
  @Column(name = "role", nullable = false)
  private AccountRole role;
  
  @NotNull
  @ColumnDefault("'ACTIVE'")
  @Lob
  @Column(name = "status", nullable = false)
  private AccountStatus status;
  
  @NotNull
  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "createdAt", nullable = false)
  private Instant createdAt;
  
}