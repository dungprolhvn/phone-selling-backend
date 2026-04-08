package ptit.ttcs.phone.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
