package ptit.ttcs.phone.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {
  
  @Value("${spring.elasticsearch.uris}")
  private String esUri;
  
  @Value("${spring.elasticsearch.username:}")
  private String username;
  
  @Value("${spring.elasticsearch.password:}")
  private String password;
  
  @Override
  public ClientConfiguration clientConfiguration() {
    try {
      // Trust ALL certificates — fine for your own controlled server
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(
          null,
          new TrustManager[]{
              new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
              }
          },
          new SecureRandom()
      );
      
      String host = esUri
          .replace("https://", "")
          .replace("http://", "");
      
      return ClientConfiguration.builder()
          .connectedTo(host)
          .usingSsl(sslContext)
          .withBasicAuth(username, password)
          .build();
      
    } catch (Exception e) {
      throw new IllegalStateException("Failed to configure Elasticsearch", e);
    }
  }
}
