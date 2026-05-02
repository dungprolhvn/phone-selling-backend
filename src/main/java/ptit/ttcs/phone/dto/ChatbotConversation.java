package ptit.ttcs.phone.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Data
public class ChatbotConversation {
  @Size(max = 50, message = "Too many conversation turns") List<Turn> turns = new ArrayList<>();
}
