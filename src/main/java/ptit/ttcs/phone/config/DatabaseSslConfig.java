package ptit.ttcs.phone.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@Configuration
public class DatabaseSslConfig {
  
  @Value("${spring.elasticsearch.ssl.truststore-password}")
  private String trustStorePassword;
  
  @PostConstruct
  public void setupTrustStore() throws Exception {
    // 1. Read from the classpath (src/main/resources/ssl/truststore.jks)
    ClassPathResource resource = new ClassPathResource("ssl/azure-mysql-truststore.jks");
    
    // 2. Create a temporary file on the host OS
    File tempStore = File.createTempFile("azure-mysql-truststore", ".jks");
    tempStore.deleteOnExit(); // Ensure it cleans up when the app stops
    
    // 3. Copy the stream to the physical temp file
    try (InputStream is = resource.getInputStream();
         FileOutputStream fos = new FileOutputStream(tempStore)) {
      is.transferTo(fos);
    }
    
    // 4. Inject the absolute path into the global JVM properties
    System.setProperty("javax.net.ssl.trustStore", tempStore.getAbsolutePath());
    System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
  }
}