package ptit.ttcs.phone.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ptit.ttcs.phone.entity.Account;

import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
  @Value("${app.jwt.secret}")
  private String secretKey;
  @Value("${app.jwt.expiration}")
  private long expiration;
  
  public String generateToken(Account account) {
    return Jwts.builder()
        .setSubject(String.valueOf(account.getId()))
        .claim("role", account.getRole().name())
        .claim("email", account.getEmail())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSigningKey())
        .compact();
  }
  
  // Extract userId from token
  public Integer extractUserId(String token) {
    return Integer.parseInt(extractClaim(token, Claims::getSubject));
  }
  
  // Extract role from token
  public String extractRole(String token) {
    return extractClaims(token).get("role", String.class);
  }
  
  // Check if token is valid (not expired, correct signature)
  public boolean isTokenValid(String token) {
    try {
      extractClaims(token); // throws if invalid
      return true;
    }
    catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }
  
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    return claimsResolver.apply(extractClaims(token));
  }
  
  private Claims extractClaims(String jwtToken) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(jwtToken)
        .getPayload();
  }
  
  private SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
  
}
