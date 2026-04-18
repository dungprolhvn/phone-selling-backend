package ptit.ttcs.phone.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
  @Size(max = 255, message = "Name must not exceed 255 characters")
  private String name;
  
  @NotNull(message = "Type is required")
  private ProductType type;
  
  @NotNull(message = "Base price is required")
  @DecimalMin(value = "0", inclusive = false, message = "Price must be greater than 0")
  @DecimalMax(value = "999999999999", message = "Price is too large")
  private BigDecimal basePrice;
  
  @Size(max = 50, message = "Specs must not exceed 50 entries")
  private Map<String, String> specs;      // { "storage": "256GB", "ram": "8GB" }
  
  @Size(max = 6553500, message = "Description must not exceed 6553500 characters")
  private String description;             // HTML content allowed
  
  private LocalDateTime releaseDate;
  
  @Size(max = 20, message = "Cannot have more than 20 images")
  private List<String> imageUrls;
  
  @NotNull(message = "Brand is required")
  private Integer brandId;
  
  @Min(value = 0, message = "Stock cannot be negative")
  private Integer stockAvailable = 0;
}
