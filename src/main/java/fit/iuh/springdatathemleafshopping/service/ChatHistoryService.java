package fit.iuh.springdatathemleafshopping.service;

import fit.iuh.springdatathemleafshopping.enitity.dto.ChatMessage;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatHistoryService {
  private static final String CHAT_HISTORY_KEY = "CHAT_HISTORY";
  private static final int MAX_HISTORY_SIZE = 50; // Limit history to 50 messages

  /**
   * Get current HTTP session
   */
  private HttpSession getCurrentSession() {
    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
    return attrs.getRequest().getSession(true);
  }

  /**
   * Get or create chat history from session
   */
  @SuppressWarnings("unchecked")
  private List<ChatMessage> getOrCreateHistory() {
    HttpSession session = getCurrentSession();
    List<ChatMessage> history = (List<ChatMessage>) session.getAttribute(CHAT_HISTORY_KEY);
    if (history == null) {
      history = new ArrayList<>();
      session.setAttribute(CHAT_HISTORY_KEY, history);
    }
    return history;
  }

  /**
   * Add a conversation (user message + bot response) to history
   */
  public void addConversation(String userMessage, String botResponse) {
    List<ChatMessage> history = getOrCreateHistory();
    ChatMessage conversation = ChatMessage.conversation(userMessage, botResponse);
    history.add(conversation);

    // Limit history size
    if (history.size() > MAX_HISTORY_SIZE) {
      history.remove(0); // Remove oldest message
    }

    HttpSession session = getCurrentSession();
    session.setAttribute(CHAT_HISTORY_KEY, history);
  }

  /**
   * Get all chat history from session
   */
  public List<ChatMessage> getHistory() {
    return new ArrayList<>(getOrCreateHistory()); // Return copy to prevent modification
  }

  /**
   * Clear chat history from session
   */
  public void clearHistory() {
    HttpSession session = getCurrentSession();
    session.removeAttribute(CHAT_HISTORY_KEY);
  }

  /**
   * Get history size
   */
  public int getHistorySize() {
    return getOrCreateHistory().size();
  }
}
