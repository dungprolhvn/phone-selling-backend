package ptit.ttcs.phone.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import ptit.ttcs.phone.enums.ProductType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ProductRequest {
  
  @NotBlank(message = "Name is required")
  private String name;
  
  @NotNull(message = "Type is required")
  private ProductType type;
  
  @NotNull(message = "Base price is required")
  @DecimalMin(value = "0", inclusive = false, message = "Price must be greater than 0")
  private BigDecimal basePrice;
  
  private Map<String, String> specs;      // { "storage": "256GB", "ram": "8GB" }
  
  private String description;             // HTML content allowed
  
  private LocalDateTime releaseDate;
  
  private List<String> imageUrls;
  
  @NotNull(message = "Brand is required")
  private Integer brandId;
  
  @Min(value = 0, message = "Stock cannot be negative")
  private Integer stockAvailable = 0;
}
