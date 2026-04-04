package ptit.ttcs.phone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ptit.ttcs.phone.enums.AccountRole;

@Data
@AllArgsConstructor
public class LoginResponse {
  private String jwtToken;
  private AccountRole role;
  private String name;
}
