package ptit.ttcs.phone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseHistoryResponse {
  @JsonProperty("content")
  private List<PurchaseHistoryItemResponse> content;
  
  @JsonProperty("currentPage")
  private Integer currentPage;
  
  @JsonProperty("totalPages")
  private Integer totalPages;
  
  @JsonProperty("totalElements")
  private Long totalElements;
  
  @JsonProperty("pageSize")
  private Integer pageSize;
  
  @JsonProperty("hasNext")
  private Boolean hasNext;
  
  @JsonProperty("hasPrevious")
  private Boolean hasPrevious;
}
