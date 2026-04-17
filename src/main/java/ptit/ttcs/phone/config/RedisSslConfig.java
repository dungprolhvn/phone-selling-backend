package ptit.ttcs.phone.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SslOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.data.redis.autoconfigure.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

@Configuration
public class RedisSslConfig {
  @Value("${spring.elasticsearch.ssl.truststore-password}")
  private String trustStorePassword;
  
  // Load the file directly from the classpath
  @Value("classpath:ssl/azure-redis-truststore.jks")
  private Resource trustStoreResource;
  
  @Bean
  public LettuceClientConfigurationBuilderCustomizer redisSslCustomizer() {
    return clientConfigurationBuilder -> {
      try {
        // 1. Initialize an empty KeyStore for the JKS format
        KeyStore trustStore = KeyStore.getInstance("JKS");
        
        // 2. Load the file stream directly from the classpath JAR into the KeyStore
        try (InputStream is = trustStoreResource.getInputStream()) {
          trustStore.load(is, trustStorePassword.toCharArray());
        }
        
        // 3. Set up the TrustManagerFactory using the loaded KeyStore
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        
        // 4. Create Lettuce-specific SslOptions using the TrustManager
        SslOptions sslOptions = SslOptions.builder()
            .trustManager(tmf)
            .build();
        
        // 5. Wrap it in ClientOptions
        ClientOptions clientOptions = ClientOptions.builder()
            .sslOptions(sslOptions)
            .build();
        
        // 6. Apply to the Spring Boot builder
        clientConfigurationBuilder
            .clientOptions(clientOptions)
            .useSsl();
        
      }
      catch (Exception e) {
        throw new IllegalStateException("Failed to configure Lettuce SSL truststore", e);
      }
    };
  }
}
