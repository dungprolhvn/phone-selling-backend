package ptit.ttcs.phone.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ptit.ttcs.phone.service.ChatbotService;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatbotController {
  
  private final ChatbotService chatbotService;
  
  @PostMapping("/message")
  public ResponseEntity<String> processChatMessage(
    Authentication authentication,
    @RequestBody @Valid @Size(min = 5, max = 65535, message = "Do dai tin nhan khong hop le") String chatMessage
  ) throws Exception {
    return ResponseEntity.ok(chatbotService.processChatMessage((int) authentication.getPrincipal(), chatMessage));
  }
}
