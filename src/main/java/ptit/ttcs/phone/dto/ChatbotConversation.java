package ptit.ttcs.phone.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.LinkedHashMap;

@Data
public class ChatbotConversation {
  @Size(max = 50, message = "Too many conversation turns")
  LinkedHashMap<String, String> turns = new LinkedHashMap<>();
}
