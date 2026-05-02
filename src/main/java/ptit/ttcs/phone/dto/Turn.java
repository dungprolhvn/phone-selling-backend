package ptit.ttcs.phone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Turn {
  private String role;    // "user" hoặc "assistant"
  private String content;
}