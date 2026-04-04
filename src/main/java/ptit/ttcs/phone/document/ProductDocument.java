package ptit.ttcs.phone.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Document(indexName = "products")
@Getter
@Setter
@NoArgsConstructor
public class ProductDocument {
  @Id
  private String id;
  
  @Field(type = FieldType.Integer)
  private Integer mysqlId;
  
  @Field(type = FieldType.Keyword)
  private String type;         // PHONE, CASE, CHARGER, CABLE
  
  @Field(type = FieldType.Text, analyzer = "standard")
  private String name;         // full-text searchable
  
  @Field(type = FieldType.Double)
  private Double basePrice;
  
  @Field(type = FieldType.Keyword)
  private String brandName;    // denormalized — avoids join at search time
  
  @Field(type = FieldType.Boolean)
  private Boolean inStock;     // true if stockAvailable > 0
  
  @Field(type = FieldType.Text, analyzer = "standard")
  private String description;  // full-text searchable
  
  // Specs stored as flat keyword fields for filtering
  @Field(type = FieldType.Keyword)
  private String storage;      // "128GB", "256GB"
  
  @Field(type = FieldType.Keyword)
  private String ram;          // "8GB", "12GB"
  
  @Field(type = FieldType.Keyword)
  private String screenSize;   // "6.1 inch"
  
  @Field(type = FieldType.Keyword)
  private String screenType;   // "OLED", "LCD", "AMOLED",...
  
  @Field(type = FieldType.Keyword)
  private String scanFrequency; // "60Hz", "120Hz"
  
  @MultiField(
      mainField = @Field(type = FieldType.Text, analyzer = "standard"),
      otherFields = {
          @InnerField(suffix = "keyword", type = FieldType.Keyword)
      }
  )
  private String[] specialFeatures; // ["Face ID", "MagSafe"]
  
  @Field(type = FieldType.Double)
  private Double averageRating;
  
  @Field(type = FieldType.Keyword)
  private String imageUrl;
}
