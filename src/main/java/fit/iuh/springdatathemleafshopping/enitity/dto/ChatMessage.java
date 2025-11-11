package fit.iuh.springdatathemleafshopping.enitity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
  private String message;
  private String response;
  private LocalDateTime timestamp;
  private boolean isUser; // true for user message, false for bot response

  public static ChatMessage userMessage(String message) {
    return new ChatMessage(message, null, LocalDateTime.now(), true);
  }

  public static ChatMessage botMessage(String response) {
    return new ChatMessage(null, response, LocalDateTime.now(), false);
  }

  public static ChatMessage conversation(String userMessage, String botResponse) {
    return new ChatMessage(userMessage, botResponse, LocalDateTime.now(), false);
  }
}
