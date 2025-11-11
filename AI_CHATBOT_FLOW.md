# 🤖 Hướng Dẫn Luồng Chạy AI Chatbot RAG

## 📋 Mục Lục

1. [Tổng Quan Kiến Trúc](#tổng-quan-kiến-trúc)
2. [Công Nghệ Sử Dụng](#công-nghệ-sử-dụng)
3. [Luồng Hoạt Động Chi Tiết](#luồng-hoạt-động-chi-tiết)
4. [Cấu Trúc Code](#cấu-trúc-code)
5. [API Endpoints](#api-endpoints)
6. [Session Management](#session-management)
7. [Troubleshooting](#troubleshooting)

---

## 🏗️ Tổng Quan Kiến Trúc

### Mô Hình RAG (Retrieval-Augmented Generation)

Chatbot sử dụng **RAG pattern** để kết hợp:

- **Retrieval**: Tìm kiếm sản phẩm từ database dựa trên từ khóa
- **Augmentation**: Tạo context từ dữ liệu sản phẩm
- **Generation**: Sử dụng Gemini AI để sinh câu trả lời tự nhiên

```
User Input → Keyword Search → Build Context → Gemini API → Response
     ↓                                                          ↓
Session Storage ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ←
```

---

## 🛠️ Công Nghệ Sử Dụng

| Thành Phần      | Công Nghệ                         | Mục Đích         |
| --------------- | --------------------------------- | ---------------- |
| **Backend**     | Spring Boot 3.5.6                 | Framework chính  |
| **AI Model**    | Gemini 1.5-pro                    | Language Model   |
| **HTTP Client** | Spring WebFlux WebClient          | Call Gemini API  |
| **Database**    | MariaDB (JPA)                     | Lưu trữ sản phẩm |
| **Session**     | HTTP Session                      | Lưu lịch sử chat |
| **Frontend**    | Vanilla JavaScript + Tailwind CSS | Chat widget UI   |
| **Security**    | Spring Security                   | CSRF protection  |

---

## 🔄 Luồng Hoạt Động Chi Tiết

### 1️⃣ User Gửi Tin Nhắn (Frontend)

**File**: `src/main/resources/templates/fragments/layout.html`

```javascript
// User nhập tin nhắn và submit form
chatForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const message = chatInput.value.trim();

  // Hiển thị tin nhắn của user
  addMessage(message, true);

  // Gọi API
  const response = await fetch("/api/chat", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ message: message }),
  });

  const data = await response.json();
  addMessage(data.response, false); // Hiển thị phản hồi bot
});
```

**Đầu vào**:

```json
{
  "message": "Tôi muốn tìm laptop giá rẻ"
}
```

---

### 2️⃣ Controller Nhận Request

**File**: `src/main/java/fit/iuh/springdatathemleafshopping/controller/ChatbotController.java`

```java
@PostMapping
public Map<String, String> chat(@RequestBody Map<String, String> request) {
    String userMessage = request.get("message");

    // Validate input
    if (userMessage == null || userMessage.trim().isEmpty()) {
        return Map.of("response", "Xin lỗi, tôi không nhận được tin nhắn.");
    }

    // Step 1: Gọi ChatbotService để xử lý
    String botResponse = chatbotService.chat(userMessage);

    // Step 2: Lưu vào session history
    chatHistoryService.addConversation(userMessage, botResponse);

    // Step 3: Trả về response
    return Map.of("response", botResponse);
}
```

**Đầu ra**:

```json
{
  "response": "Tôi tìm thấy 3 laptop giá rẻ cho bạn: Laptop Dell... Laptop HP..."
}
```

---

### 3️⃣ ChatbotService - RAG Pipeline

**File**: `src/main/java/fit/iuh/springdatathemleafshopping/service/ChatbotService.java`

#### **Bước 3.1: Retrieve - Tìm Sản Phẩm Liên Quan**

```java
private List<Product> retrieveRelevantProducts(String query) {
    String[] keywords = query.toLowerCase().split("\\s+");
    List<Product> allProducts = productRepository.findAll();

    // Filter products by keyword matching
    return allProducts.stream()
        .filter(product -> {
            String productText = (product.getName() + " " +
                                 product.getDescription()).toLowerCase();
            return Arrays.stream(keywords)
                .anyMatch(productText::contains);
        })
        .limit(5) // Lấy tối đa 5 sản phẩm
        .collect(Collectors.toList());
}
```

**Ví dụ**:

- Input: `"laptop giá rẻ"`
- Keywords: `["laptop", "giá", "rẻ"]`
- Kết quả: Danh sách các Product có name/description chứa "laptop"

---

#### **Bước 3.2: Augment - Xây Dựng Context**

```java
private String buildProductContext(List<Product> products) {
    if (products.isEmpty()) {
        return "Không có sản phẩm nào được tìm thấy.";
    }

    StringBuilder context = new StringBuilder("Danh sách sản phẩm:\n");
    for (Product p : products) {
        context.append("- Tên: ").append(p.getName())
               .append(", Giá: ").append(p.getPrice()).append(" VNĐ")
               .append(", Mô tả: ").append(p.getDescription())
               .append(", Tình trạng: ")
               .append(p.isInStock() ? "Còn hàng" : "Hết hàng")
               .append("\n");
    }
    return context.toString();
}
```

**Output Example**:

```
Danh sách sản phẩm:
- Tên: Laptop Dell Inspiron 15, Giá: 12000000 VNĐ, Mô tả: Laptop văn phòng..., Tình trạng: Còn hàng
- Tên: Laptop HP Pavilion, Giá: 15000000 VNĐ, Mô tả: Laptop gaming..., Tình trạng: Còn hàng
```

---

#### **Bước 3.3: Build Prompt cho Gemini**

```java
private String buildPrompt(String context, String userMessage) {
    return String.format(
        "Bạn là trợ lý mua sắm thông minh cho một cửa hàng điện tử.\n\n" +
        "CONTEXT (dữ liệu sản phẩm):\n%s\n\n" +
        "USER QUESTION: %s\n\n" +
        "Hãy trả lời câu hỏi của khách hàng dựa trên context trên. " +
        "Nếu không có sản phẩm phù hợp, hãy gợi ý khách hàng tìm kiếm khác. " +
        "Trả lời bằng tiếng Việt, thân thiện và chuyên nghiệp.",
        context, userMessage
    );
}
```

---

#### **Bước 3.4: Generate - Gọi Gemini API**

```java
private String callGeminiAPI(String prompt) {
    String apiKey = geminiProperties.getApiKey();
    String model = geminiProperties.getChat().getOptions().getModel();
    String url = String.format(
        "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
        model, apiKey
    );

    // Build request body
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contents", List.of(
        Map.of("parts", List.of(Map.of("text", prompt)))
    ));
    requestBody.put("generationConfig", Map.of(
        "temperature", geminiProperties.getChat().getOptions().getTemperature(),
        "maxOutputTokens", 1000
    ));

    // Call API using WebClient
    WebClient webClient = webClientBuilder.build();

    String responseBody = webClient.post()
        .uri(url)
        .header("Content-Type", "application/json")
        .bodyValue(requestBody)
        .retrieve()
        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                  response -> response.bodyToMono(String.class)
                      .map(body -> new RuntimeException("HTTP Error: " + body)))
        .bodyToMono(String.class)
        .block();

    // Parse response
    return parseGeminiResponse(responseBody);
}
```

**Request Example**:

```json
{
  "contents": [
    {
      "parts": [
        {
          "text": "Bạn là trợ lý mua sắm...\n\nCONTEXT:\n- Laptop Dell...\n\nUSER: tìm laptop giá rẻ"
        }
      ]
    }
  ],
  "generationConfig": {
    "temperature": 0.8,
    "maxOutputTokens": 1000
  }
}
```

**Response Example**:

```json
{
  "candidates": [
    {
      "content": {
        "parts": [
          {
            "text": "Tôi tìm thấy 2 laptop phù hợp với ngân sách của bạn:\n\n1. Laptop Dell Inspiron 15..."
          }
        ]
      }
    }
  ]
}
```

---

### 4️⃣ Session History - Lưu Trữ Lịch Sử

**File**: `src/main/java/fit/iuh/springdatathemleafshopping/service/ChatHistoryService.java`

```java
@Service
public class ChatHistoryService {
    private static final String SESSION_KEY = "CHAT_HISTORY";
    private static final int MAX_HISTORY_SIZE = 50;

    public void addConversation(String userMessage, String botResponse) {
        HttpSession session = getCurrentSession();

        @SuppressWarnings("unchecked")
        List<ChatMessage> history = (List<ChatMessage>)
            session.getAttribute(SESSION_KEY);

        if (history == null) {
            history = new ArrayList<>();
        }

        // Add new conversation
        history.add(ChatMessage.conversation(userMessage, botResponse));

        // Limit history size (FIFO)
        if (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0);
        }

        session.setAttribute(SESSION_KEY, history);
    }
}
```

**Session Structure**:

```json
{
  "JSESSIONID": "ABC123XYZ",
  "CHAT_HISTORY": [
    {
      "message": "Tôi muốn tìm laptop",
      "response": "Tôi tìm thấy 5 laptop...",
      "timestamp": "2025-11-11T10:30:00",
      "isUser": false
    }
    // ... more messages
  ]
}
```

---

### 5️⃣ Load History khi Mở Chat (Frontend)

```javascript
let historyLoaded = false;

async function loadChatHistory() {
  if (historyLoaded) return;

  try {
    const response = await fetch("/api/chat/history");
    const data = await response.json();

    if (data.history && data.history.length > 0) {
      // Clear welcome message
      chatMessages.innerHTML = "";

      // Render all previous conversations
      data.history.forEach((conv) => {
        addMessage(conv.message, true); // User message
        addMessage(conv.response, false); // Bot response
      });
    }

    historyLoaded = true;
  } catch (error) {
    console.error("Failed to load history:", error);
  }
}

// Load khi mở chat
chatToggle.addEventListener("click", () => {
  if (!chatBox.classList.contains("hidden")) {
    loadChatHistory(); // Load history on first open
  }
});
```

---

## 📁 Cấu Trúc Code

```
src/main/java/fit/iuh/springdatathemleafshopping/
├── controller/
│   └── ChatbotController.java        # REST API endpoints
├── service/
│   ├── ChatbotService.java           # RAG logic + Gemini API
│   └── ChatHistoryService.java       # Session management
├── enitity/dto/
│   └── ChatMessage.java              # DTO cho tin nhắn
├── config/
│   ├── GeminiProperties.java         # Config binding
│   ├── WebClientConfig.java          # WebClient bean
│   └── SecurityConfiguration.java    # Security bypass cho /api/chat/**
└── repository/
    └── ProductRepository.java        # JPA repository

src/main/resources/
├── application.properties            # Gemini API key config
└── templates/fragments/
    └── layout.html                   # Chat widget UI + JavaScript
```

---

## 🔌 API Endpoints

### 1. Chat Endpoint (POST)

```http
POST /api/chat
Content-Type: application/json

{
  "message": "Tôi muốn mua điện thoại Samsung"
}
```

**Response**:

```json
{
  "response": "Tôi tìm thấy 3 điện thoại Samsung phù hợp: ..."
}
```

---

### 2. Get History (GET)

```http
GET /api/chat/history
```

**Response**:

```json
{
  "history": [
    {
      "message": "Tôi muốn mua laptop",
      "response": "Tôi tìm thấy...",
      "timestamp": "2025-11-11T10:30:00",
      "isUser": false
    }
  ],
  "count": 5
}
```

---

### 3. Clear History (DELETE)

```http
DELETE /api/chat/history
```

**Response**:

```json
{
  "message": "Chat history cleared"
}
```

---

### 4. Test Endpoint (GET)

```http
GET /api/chat/test
```

**Response**:

```json
{
  "apiKeyConfigured": true,
  "apiKeyPrefix": "AIzaSyBB0B",
  "model": "gemini-1.5-pro",
  "temperature": 0.8,
  "historySize": 5
}
```

---

## 💾 Session Management

### Vòng Đời Session

1. **Tạo Session**: Khi user gửi tin nhắn đầu tiên → Server tạo JSESSIONID
2. **Lưu Trữ**: Mỗi conversation được lưu vào `List<ChatMessage>` trong session
3. **Persist**: Session tồn tại khi user navigate giữa các trang (cookie JSESSIONID)
4. **Expire**: Session mất khi:
   - User đóng browser
   - Timeout (mặc định 30 phút)
   - User click clear history

### Session Configuration

```properties
# application.properties
server.servlet.session.timeout=30m
server.servlet.session.cookie.max-age=1800
```

---

## 🔒 Security Configuration

### CSRF Exception cho Chat API

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/chat/**").permitAll() // Allow unauthenticated
            .anyRequest().authenticated()
        )
        .csrf(csrf -> csrf
            .ignoringRequestMatchers("/api/chat/**") // Disable CSRF for chat
        );
    return http.build();
}
```

**Lý do**: JavaScript fetch() không tự động gửi CSRF token nên cần bypass cho `/api/chat/**`

---

## 🐛 Troubleshooting

### ❌ Lỗi: "Request method 'POST' is not supported"

**Nguyên nhân**: CSRF protection chặn POST request

**Giải pháp**:

```java
.csrf(csrf -> csrf.ignoringRequestMatchers("/api/chat/**"))
```

---

### ❌ Lỗi: "Unknown property: spring.ai.gemini.chat.options.model"

**Nguyên nhân**: Config binding không match cấu trúc properties

**Giải pháp**: Tạo nested class structure

```java
@ConfigurationProperties(prefix = "spring.ai.gemini")
public class GeminiProperties {
    private Chat chat;

    public static class Chat {
        private Options options;

        public static class Options {
            private String model;
            private Double temperature;
        }
    }
}
```

---

### ❌ Lỗi: "Xin lỗi, đã xảy ra lỗi" (Gemini API Error)

**Debug Steps**:

1. Check API key: `GET /api/chat/test`
2. Check logs: Look for "DEBUG - " prefix
3. Check error response: `WebClient HTTP Error: {statusCode}`
4. Verify API quota: Visit [Google AI Studio](https://aistudio.google.com/)

---

### ❌ Chat history không load

**Kiểm tra**:

1. Browser console: `Failed to load history: ...`
2. Network tab: Check `/api/chat/history` request
3. Session cookie: JSESSIONID có tồn tại không?
4. Backend logs: Check `chatHistoryService.getHistory()` được gọi chưa

---

## 📊 Sequence Diagram

```
User              Frontend           Controller         ChatbotService      Database      Gemini API      Session
 |                   |                    |                    |                |              |             |
 |--- Type message ->|                    |                    |                |              |             |
 |                   |--- POST /api/chat->|                    |                |              |             |
 |                   |                    |--- chat(msg) ----->|                |              |             |
 |                   |                    |                    |--- findAll --->|              |             |
 |                   |                    |                    |<-- products ---|              |             |
 |                   |                    |                    |--- buildContext|              |             |
 |                   |                    |                    |--- POST ---------------------->|             |
 |                   |                    |                    |<-- AI response ---------------|             |
 |                   |                    |<-- bot response ---|                |              |             |
 |                   |                    |--- addConversation ---------------------------------------->|     |
 |                   |                    |                    |                |              |             |
 |                   |<-- JSON response --|                    |                |              |             |
 |<-- Display bot ---|                    |                    |                |              |             |
 |                   |                    |                    |                |              |             |
 |--- Navigate page->|                    |                    |                |              |             |
 |--- Open chat ---->|                    |                    |                |              |             |
 |                   |--- GET /history -->|                    |                |              |             |
 |                   |                    |--- getHistory ---------------------------------------->|     |
 |                   |                    |<-- history list ------------------------------------------|     |
 |                   |<-- JSON history ---|                    |                |              |             |
 |<-- Display old ---|                    |                    |                |              |             |
```

---

## 🎯 Kết Luận

### Ưu Điểm của Kiến Trúc

✅ **RAG Pattern**: Kết hợp data thật với AI → Câu trả lời chính xác hơn  
✅ **Session-based**: Persist conversation cho guest users  
✅ **Scalable**: Dễ mở rộng thêm features (rating, feedback, ...)  
✅ **User-friendly**: Floating widget không ảnh hưởng UX  
✅ **Secure**: CSRF protection với bypass hợp lý

### Hạn Chế & Cải Tiến Tương Lai

⚠️ **Keyword matching đơn giản** → Nâng cấp lên vector search (embeddings)  
⚠️ **Không có authentication** → Thêm user ID để track history per user  
⚠️ **API key hardcoded trong .env** → Sử dụng secrets manager  
⚠️ **Không cache response** → Thêm Redis cache cho frequent queries

---

## 📚 Tài Liệu Tham Khảo

- [Gemini API Documentation](https://ai.google.dev/docs)
- [Spring WebFlux WebClient](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html)
- [RAG Pattern Explained](https://www.promptingguide.ai/techniques/rag)
- [HTTP Session Management](https://docs.spring.io/spring-session/reference/guides/boot-redis.html)

---

**Last Updated**: November 11, 2025  
**Version**: 1.0  
**Author**: AI Chatbot Development Team
