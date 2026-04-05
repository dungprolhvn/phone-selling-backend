package ptit.ttcs.phone.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BrandResponse {
  private Integer id;
  private String name;
  private Map<String, Object> logoImageUrls;
}
