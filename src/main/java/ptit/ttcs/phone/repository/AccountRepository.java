package ptit.ttcs.phone.repository;

import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ptit.ttcs.phone.entity.Account;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
  Optional<Account> findByEmail(String email);
  
  boolean existsByEmail(String email);
  
  boolean existsByPhone(@Pattern(regexp = "^[0-9]{10}$", message = "So dien thoai khong hop le") String phone);
}
