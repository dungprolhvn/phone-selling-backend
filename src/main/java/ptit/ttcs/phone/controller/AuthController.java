package ptit.ttcs.phone.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ptit.ttcs.phone.dto.LoginRequest;
import ptit.ttcs.phone.dto.LoginResponse;
import ptit.ttcs.phone.dto.RegisterRequest;
import ptit.ttcs.phone.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  
  private final AuthService authService;
  
  @PostMapping("/register")
  public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest request) {
    authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
  
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }
  
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(Authentication authentication) {
    Integer userId = (Integer) authentication.getPrincipal();
    authService.logout(userId);
    return ResponseEntity.ok().build();
  }
}
