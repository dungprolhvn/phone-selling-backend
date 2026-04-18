package ptit.ttcs.phone.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
  @NotBlank(message = "Email khong duoc bo trong")
  @Email
  @Size(max = 255, message = "Email khong duoc qua 255 ki tu")
  private String email;
  @NotBlank(message = "Password khong duoc bo trong")
  @Size(min = 8, max = 24, message = "Mat khau phai tu 8 den 24 ki tu.")
  private String password;
}
