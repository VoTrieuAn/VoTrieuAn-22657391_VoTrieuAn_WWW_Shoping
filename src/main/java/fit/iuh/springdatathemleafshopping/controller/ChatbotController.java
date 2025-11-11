package fit.iuh.springdatathemleafshopping.controller;

import fit.iuh.springdatathemleafshopping.config.GeminiProperties;
import fit.iuh.springdatathemleafshopping.enitity.dto.ChatMessage;
import fit.iuh.springdatathemleafshopping.service.ChatHistoryService;
import fit.iuh.springdatathemleafshopping.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatbotController {
  private final ChatbotService chatbotService;
  private final GeminiProperties geminiProperties;
  private final ChatHistoryService chatHistoryService;

  @PostMapping
  public Map<String, String> chat(@RequestBody Map<String, String> request) {
    String userMessage = request.get("message");

    if (userMessage == null || userMessage.trim().isEmpty()) {
      Map<String, String> response = new HashMap<>();
      response.put("response", "Xin lỗi, tôi không nhận được tin nhắn của bạn.");
      return response;
    }

    // Get bot response
    String botResponse = chatbotService.chat(userMessage);

    // Save to session history
    chatHistoryService.addConversation(userMessage, botResponse);

    Map<String, String> response = new HashMap<>();
    response.put("response", botResponse);
    return response;
  }

  @GetMapping("/history")
  public Map<String, Object> getHistory() {
    List<ChatMessage> history = chatHistoryService.getHistory();
    Map<String, Object> response = new HashMap<>();
    response.put("history", history);
    response.put("count", history.size());
    return response;
  }

  @DeleteMapping("/history")
  public Map<String, String> clearHistory() {
    chatHistoryService.clearHistory();
    Map<String, String> response = new HashMap<>();
    response.put("message", "Chat history cleared");
    return response;
  }

  @GetMapping("/test")
  public Map<String, Object> test() {
    Map<String, Object> response = new HashMap<>();
    String apiKey = geminiProperties.getApiKey();
    response.put("apiKeyConfigured", apiKey != null && !apiKey.isEmpty() && !apiKey.startsWith("${"));
    response.put("apiKeyPrefix", apiKey != null ? apiKey.substring(0, Math.min(10, apiKey.length())) : "null");
    response.put("model", geminiProperties.getChat().getOptions().getModel());
    response.put("temperature", geminiProperties.getChat().getOptions().getTemperature());
    response.put("historySize", chatHistoryService.getHistorySize());
    return response;
  }
}
