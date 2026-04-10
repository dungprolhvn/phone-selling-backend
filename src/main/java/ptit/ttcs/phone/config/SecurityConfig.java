package ptit.ttcs.phone.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ptit.ttcs.phone.security.JwtAuthFilter;

import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final JwtAuthFilter jwtAuthFilter;
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
          .requestMatchers("/api/products/infoUpdate/**").hasRole("ADMIN")
          .requestMatchers("/api/products/stockUpdate").hasRole("WAREHOUSE_STAFF")
          .requestMatchers("/api/orders/statusUpdate").hasRole("WAREHOUSE_STAFF")
            .requestMatchers(
                "/actuator/**",
                "/api/auth/**",
                "/api/products/**",
                "/api/brands",
                "/api/promotions/ongoing",
                "/api/brands/**",
                "/api/cart/**",
                "/api/orders/status",
                "/api/payments/vnpay/ipn", // payment ipn
                // api docs
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
            ).permitAll()
            
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            
            .requestMatchers("/api/warehouse/**").hasRole("WAREHOUSE_STAFF")
            
            .anyRequest().authenticated()
        )
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint((request, response, authException) -> {
              response.setContentType("application/json");
              response.setStatus(401);
              response.getWriter().write(
                  "{\"status\":401,\"message\":\"Authentication required\"}"
              );
            })
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              response.setContentType("application/json");
              response.setStatus(403);
              response.getWriter().write(
                  "{\"status\":403,\"message\":\"Access denied\"}"
              );
            })
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
  
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    
    // Allowed origins — your Nuxt dev server and production domain
    config.setAllowedOrigins(List.of(
        "http://localhost:3000",    // Nuxt dev
        "http://localhost:3001"     // in case you run on alternate port
    ));
    
    // Allowed HTTP methods
    config.setAllowedMethods(List.of(
        "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
    ));
    
    // Allowed headers — include Authorization for JWT
    config.setAllowedHeaders(List.of(
        "Authorization",
        "Content-Type",
        "X-Guest-Id"        // your guest cart header
    ));
    
    // Allow credentials — required for cookies (if you use HTTP-only cookie for JWT)
    config.setAllowCredentials(true);
    
    // Cache preflight response for 1 hour — reduces OPTIONS requests
    config.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
  
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
  
  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }
}
