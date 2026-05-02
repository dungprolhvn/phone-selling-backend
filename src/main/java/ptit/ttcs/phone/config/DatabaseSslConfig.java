package ptit.ttcs.phone.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Enumeration;

@Configuration
public class DatabaseSslConfig {
  
  @Value("${spring.elasticsearch.ssl.truststore-password}")
  private String trustStorePassword;
  
  @PostConstruct
  public void setupTrustStore() throws Exception {
    // 1. Load custom MySQL truststore
    KeyStore mergedStore = KeyStore.getInstance("JKS");
    try (InputStream is = new ClassPathResource("ssl/azure-mysql-truststore.jks").getInputStream()) {
      mergedStore.load(is, trustStorePassword.toCharArray());
    }
    
    // 2. Load JVM cacerts gốc (chứa Google CA, Let's Encrypt, ...)
    String cacertsPath = System.getProperty("java.home") + "/lib/security/cacerts";
    KeyStore defaultStore = KeyStore.getInstance("JKS");
    try (InputStream is = new FileInputStream(cacertsPath)) {
      defaultStore.load(is, "changeit".toCharArray());
    }
    
    // 3. Merge tất cả CA từ cacerts vào mergedStore
    Enumeration<String> aliases = defaultStore.aliases();
    while (aliases.hasMoreElements()) {
      String alias = aliases.nextElement();
      if (!mergedStore.containsAlias(alias)) {
        mergedStore.setCertificateEntry(alias, defaultStore.getCertificate(alias));
      }
    }
    
    // 4. Ghi merged store ra temp file rồi set global
    File tempStore = File.createTempFile("merged-truststore", ".jks");
    tempStore.deleteOnExit();
    try (FileOutputStream fos = new FileOutputStream(tempStore)) {
      mergedStore.store(fos, trustStorePassword.toCharArray());
    }
    
    System.setProperty("javax.net.ssl.trustStore", tempStore.getAbsolutePath());
    System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
  }
}