package fit.iuh.springdatathemleafshopping.service;

import fit.iuh.springdatathemleafshopping.config.GeminiProperties;
import fit.iuh.springdatathemleafshopping.enitity.Product;
import fit.iuh.springdatathemleafshopping.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotService {
  private final ProductRepository productRepository;
  private final GeminiProperties geminiProperties;
  private final WebClient.Builder webClientBuilder;

  /**
   * RAG approach: retrieve relevant products from DB, build context, send to
   * Gemini
   */
  public String chat(String userMessage) {
    // Step 1: Retrieve relevant products (simple keyword search for demo)
    List<Product> relevantProducts = retrieveRelevantProducts(userMessage);

    // Step 2: Build context from retrieved products
    String context = buildProductContext(relevantProducts);

    // Step 3: Create prompt with context and user question
    String prompt = buildPrompt(context, userMessage);

    // Step 4: Call Gemini API
    try {
      String response = callGeminiAPI(prompt);
      return response;
    } catch (Exception e) {
      System.err.println("Chat error: " + e.getMessage());
      // Fallback response when API fails
      if (relevantProducts.isEmpty()) {
        return "Xin lỗi, tôi không tìm thấy sản phẩm nào phù hợp với từ khóa của bạn. Bạn có thể thử tìm kiếm với từ khóa khác hoặc xem danh sách sản phẩm tại trang chủ.";
      } else {
        StringBuilder fallback = new StringBuilder("Dựa trên tìm kiếm, tôi tìm thấy các sản phẩm sau:\n\n");
        for (Product p : relevantProducts) {
          fallback.append("• ").append(p.getName())
              .append(" - Giá: ").append(p.getPrice()).append(" VNĐ")
              .append(p.isInStock() ? " (Còn hàng)" : " (Hết hàng)")
              .append("\n");
        }
        fallback.append("\nBạn có thể xem chi tiết tại trang sản phẩm!");
        return fallback.toString();
      }
    }
  }

  /**
   * Retrieve products from DB based on keyword matching (simple RAG retrieval)
   */
  private List<Product> retrieveRelevantProducts(String query) {
    // Extract potential keywords (simple split by space)
    String[] keywords = query.toLowerCase().split("\\s+");

    // Get all products and filter by keyword matching
    List<Product> allProducts = productRepository.findAll();

    return allProducts.stream()
        .filter(p -> {
          String productName = p.getName().toLowerCase();
          for (String keyword : keywords) {
            if (productName.contains(keyword)) {
              return true;
            }
          }
          return false;
        })
        .limit(5) // Limit to top 5 relevant products
        .collect(Collectors.toList());
  }

  /**
   * Build context string from retrieved products
   */
  private String buildProductContext(List<Product> products) {
    if (products.isEmpty()) {
      return "Không tìm thấy sản phẩm liên quan trong cơ sở dữ liệu.";
    }

    StringBuilder context = new StringBuilder("Danh sách sản phẩm có sẵn:\n");
    for (Product p : products) {
      context.append(String.format("- %s (ID: %d, Giá: %s VNĐ, %s, Tồn kho: %d)\n",
          p.getName(),
          p.getId(),
          p.getPrice(),
          p.isInStock() ? "Còn hàng" : "Hết hàng",
          p.getStock() != null ? p.getStock() : 0));
    }
    return context.toString();
  }

  /**
   * Build prompt for Gemini with RAG context
   */
  private String buildPrompt(String context, String userMessage) {
    return String.format(
        "Bạn là trợ lý mua sắm thông minh. Dựa trên thông tin sản phẩm sau:\n\n%s\n\n" +
            "Hãy trả lời câu hỏi của khách hàng một cách thân thiện và hữu ích: %s\n\n" +
            "Nếu có sản phẩm phù hợp, hãy giới thiệu và đề xuất. Nếu không có, hãy xin lỗi và đề xuất khách hàng tìm kiếm từ khóa khác.",
        context,
        userMessage);
  }

  /**
   * Call Gemini API using WebClient
   */
  private String callGeminiAPI(String prompt) {
    String apiKey = geminiProperties.getApiKey();
    String model = geminiProperties.getChat().getOptions().getModel();

    System.out.println(
        "DEBUG - API Key: " + (apiKey != null ? (apiKey.substring(0, Math.min(10, apiKey.length())) + "...") : "null"));
    System.out.println("DEBUG - Model: " + model);

    if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("${")) {
      return "Xin lỗi, chatbot chưa được cấu hình đúng (thiếu API key). Vui lòng liên hệ quản trị viên.";
    }

    try {
      WebClient webClient = webClientBuilder.baseUrl("https://generativelanguage.googleapis.com").build();

      Map<String, Object> requestBody = new HashMap<>();
      Map<String, Object> content = new HashMap<>();
      Map<String, String> part = new HashMap<>();
      part.put("text", prompt);
      content.put("parts", List.of(part));
      requestBody.put("contents", List.of(content));

      System.out.println("DEBUG - Calling Gemini API...");
      System.out.println("DEBUG - Request body: " + requestBody);

      @SuppressWarnings("unchecked")
      Map<String, Object> result = webClient.post()
          .uri(uriBuilder -> uriBuilder
              .path("/v1beta/models/{model}:generateContent")
              .queryParam("key", apiKey)
              .build(model))
          .bodyValue(requestBody)
          .retrieve()
          .onStatus(
              status -> status.is4xxClientError() || status.is5xxServerError(),
              response -> response.bodyToMono(String.class).map(body -> {
                System.err.println("HTTP Error: " + response.statusCode() + " - " + body);
                return new RuntimeException("HTTP Error " + response.statusCode() + ": " + body);
              }))
          .bodyToMono(Map.class)
          .block();

      System.out.println("DEBUG - Response received: " + (result != null ? result.keySet() : "null"));

      // Parse response
      if (result != null) {
        // Check for error
        if (result.containsKey("error")) {
          @SuppressWarnings("unchecked")
          Map<String, Object> error = (Map<String, Object>) result.get("error");
          String errorMessage = (String) error.get("message");
          Integer errorCode = (Integer) error.get("code");
          System.err.println("Gemini API Error [" + errorCode + "]: " + errorMessage);
          return "Xin lỗi, API trả về lỗi: " + errorMessage;
        }

        if (result.containsKey("candidates")) {
          @SuppressWarnings("unchecked")
          List<Map<String, Object>> candidates = (List<Map<String, Object>>) result.get("candidates");
          if (!candidates.isEmpty()) {
            Map<String, Object> candidate = candidates.get(0);
            @SuppressWarnings("unchecked")
            Map<String, Object> contentMap = (Map<String, Object>) candidate.get("content");
            if (contentMap != null && contentMap.containsKey("parts")) {
              @SuppressWarnings("unchecked")
              List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
              if (!parts.isEmpty() && parts.get(0).containsKey("text")) {
                String responseText = (String) parts.get(0).get("text");
                System.out.println("DEBUG - Success! Response length: " + responseText.length());
                return responseText;
              }
            }
          }
        }
      }

      System.err.println("Unable to parse Gemini response: " + result);
      return "Xin lỗi, tôi không thể xử lý câu hỏi của bạn lúc này. Response format không đúng.";

    } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
      System.err.println("WebClient HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
      return "Xin lỗi, lỗi kết nối API [" + e.getStatusCode() + "]: " + e.getMessage();
    } catch (Exception e) {
      System.err.println("ERROR calling Gemini API: " + e.getClass().getName() + " - " + e.getMessage());
      e.printStackTrace();
      return "Xin lỗi, đã xảy ra lỗi: " + e.getMessage();
    }
  }
}
