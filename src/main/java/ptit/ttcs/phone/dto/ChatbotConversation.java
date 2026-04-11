package ptit.ttcs.phone.dto;

import lombok.Data;

import java.util.LinkedHashMap;

@Data
public class ChatbotConversation {
  LinkedHashMap<String, String> turns = new LinkedHashMap<>();
}
