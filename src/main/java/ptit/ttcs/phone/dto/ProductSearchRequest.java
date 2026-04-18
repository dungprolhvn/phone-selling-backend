package ptit.ttcs.phone.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductSearchRequest {
  @Size(max = 200, message = "Keyword must not exceed 200 characters")
  private String keyword;       // "iphone 16"

  @Size(max = 50, message = "Type must not exceed 50 characters")
  private String type;          // "PHONE"

  @Size(max = 100, message = "Brand name must not exceed 100 characters")
  private String brandName;     // "Apple"

  @DecimalMin(value = "0", message = "Min price must be >= 0")
  private Double minPrice;      // 10000000

  @DecimalMax(value = "999999999999", message = "Max price is too large")
  private Double maxPrice;      // 30000000

  private Boolean inStockOnly;  // true

  @Size(max = 50, message = "Storage must not exceed 50 characters")
  private String storage;       // "256GB"

  @Size(max = 50, message = "RAM must not exceed 50 characters")
  private String ram;           // "8GB"

  @Size(max = 50, message = "Screen type must not exceed 50 characters")
  private String screenType;    // "OLED"

  @Size(max = 20, message = "Scan frequency must not exceed 20 characters")
  private String scanFrequency; // "120Hz"

  @Pattern(regexp = "^(basePrice|averageRating|name|createdAt)?$",
           message = "Invalid sort field")
  private String sortBy;        // "basePrice", "averageRating"

  @Min(value = 0, message = "Page must be >= 0")
  private Integer page;         // 0

  @Min(value = 1, message = "Size must be >= 1")
  @Max(value = 100, message = "Size must not exceed 100")
  private Integer size;         // 20
}
