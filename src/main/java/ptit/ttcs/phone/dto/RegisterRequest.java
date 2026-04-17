package ptit.ttcs.phone.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
  @NotBlank(message = "Email khong duoc bo trong")
  @Email
  @Size(max = 255, message = "Email khong duoc qua 255 ki tu")
  private String email;
  
  @NotBlank(message = "Ten khong duoc bo trong")
  @Size(min = 1, max = 255, message = "Ten khong duoc qua 255 ki tu.")
  private String name;
  
  @Pattern(regexp = "^[0-9]{10}$", message = "So dien thoai khong hop le")
  private String phone;
  
  @NotBlank(message = "Password khong duoc bo trong")
  @Size(min = 8, max = 24, message = "Mat khau phai tu 8 den 24 ki tu.")
  private String password;
  
  @NotBlank(message = "Password khong duoc bo trong")
  @Size(min = 8, max = 24, message = "Mat khau phai tu 8 den 24 ki tu.")
  private String confirmation;
}
