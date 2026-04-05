package ptit.ttcs.phone.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ptit.ttcs.phone.dto.Cart;
import ptit.ttcs.phone.dto.LoginRequest;
import ptit.ttcs.phone.dto.LoginResponse;
import ptit.ttcs.phone.dto.RegisterRequest;
import ptit.ttcs.phone.entity.Account;
import ptit.ttcs.phone.enums.AccountRole;
import ptit.ttcs.phone.enums.AccountStatus;
import ptit.ttcs.phone.exception.ConflictException;
import ptit.ttcs.phone.exception.ForbiddenException;
import ptit.ttcs.phone.exception.UnauthorizedException;
import ptit.ttcs.phone.repository.AccountRepository;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;
  
  @Value("${app.jwt.expiration}")
  private long expiration;
  public void register(RegisterRequest request) {
    //
    if (!request.getPassword().equals(request.getConfirmation())) {
      throw new ConflictException("Passwords do not match");
    }
    // Check duplicate email/phone
    if (accountRepository.existsByEmail(request.getEmail())) {
      throw new ConflictException("Email already exists");
    }
    if (accountRepository.existsByPhone(request.getPhone())) {
      throw new ConflictException("Phone already exists");
    }
    
    Account account = new Account();
    account.setName(request.getName());
    account.setEmail(request.getEmail());
    account.setPhone(request.getPhone());
    account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    account.setRole(AccountRole.USER);
    account.setStatus(AccountStatus.ACTIVE);
    
    accountRepository.save(account);
  }
  
  public LoginResponse login(LoginRequest request, String guestId) {
    // Find account
    Account account = accountRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
    
    // Check banned
    if (account.getStatus() == AccountStatus.BANNED) {
      throw new ForbiddenException("Account is banned");
    }
    
    // Verify password
    if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
      throw new UnauthorizedException("Invalid credentials");
    }
    
    // Generate JWT
    String token = jwtService.generateToken(account);
    
    // Store in Redis — key: session:{userId}, value: token, TTL: same as token
    String redisKey = "session:" + account.getId();
    redisTemplate.opsForValue().set(
        redisKey,
        token,
        expiration,
        TimeUnit.MILLISECONDS
    );
    
    // merge cart
    if (guestId != null) {
      Cart guestCart, userCart;
      try {
        guestCart = objectMapper.readValue(
            redisTemplate.opsForValue().get(String.format("cart:guest:%s", guestId)),
            Cart.class
        );
      }
      catch (Exception e) {
        guestCart = null;
      }
      try {
        userCart = objectMapper.readValue(
            redisTemplate.opsForValue().get(String.format("cart:user:%s", account.getId())),
            Cart.class
        );
      }
      catch (Exception e) {
        userCart = null;
      }
      try {
        if (guestCart != null && !guestCart.getProducts().isEmpty()) {
          if (userCart != null) {
            userCart.merge(guestCart);
          }
          else {
            userCart = guestCart;
          }
          redisTemplate.opsForValue().set(
              String.format("cart:user:%s", account.getId()),
              objectMapper.writeValueAsString(userCart),
              Duration.ofDays(30)
          );
          redisTemplate.delete(String.format("cart:guest:%s", guestId));
        }
      }
      catch (JsonProcessingException | IllegalArgumentException e) {
        log.error("Error merging cart of user " + account.getEmail() + e.getMessage());
      }
    }
    return new LoginResponse(token, account.getRole(), account.getName());
  }
  
  public void logout(Integer userId) {
    redisTemplate.delete("session:" + userId);
  }
}
