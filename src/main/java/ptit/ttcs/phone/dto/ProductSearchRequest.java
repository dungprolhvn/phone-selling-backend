package ptit.ttcs.phone.dto;

import lombok.Data;

@Data
public class ProductSearchRequest {
  private String keyword;       // "iphone 16"
  private String type;          // "PHONE"
  private String brandName;     // "Apple"
  private Double minPrice;      // 10000000
  private Double maxPrice;      // 30000000
  private Boolean inStockOnly;  // true
  private String storage;       // "256GB"
  private String ram;           // "8GB"
  private String screenType;    // "OLED"
  private String scanFrequency; // "120Hz"
  private String sortBy;        // "basePrice", "averageRating"
  private Integer page;         // 0
  private Integer size;         // 20
}
