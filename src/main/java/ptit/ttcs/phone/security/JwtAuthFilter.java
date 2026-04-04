package ptit.ttcs.phone.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ptit.ttcs.phone.service.JwtService;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final RedisTemplate<String, String> redisTemplate;
  
  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    String authHeader = request.getHeader("Authorization");
    // no jwt token, treat as anonymous
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }
    String token = authHeader.substring(7);
    // treat invalid token as anonymous
    if (!jwtService.isTokenValid(token)) {
      filterChain.doFilter(request, response);
      return;
    }
    // check for blacklisted token
    String redisKey = "session:" + jwtService.extractUserId(token);
    String storedToken = redisTemplate.opsForValue().get(redisKey);
    if (storedToken == null || !storedToken.equals(token)) {
      filterChain.doFilter(request, response);
      return;
    }
    // authenticate
    String role = jwtService.extractRole(token);
    Integer userId = jwtService.extractUserId(token);
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            userId,                          // principal — available as getPrincipal() in controllers
            null,                            // credentials — not needed after auth
            List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    SecurityContextHolder.getContext().setAuthentication(authentication);
    filterChain.doFilter(request, response);
    }
    
}
