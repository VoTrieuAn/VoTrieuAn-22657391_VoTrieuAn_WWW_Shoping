# RAG Chatbot Feature

## Tổng quan

Chatbot sử dụng công nghệ RAG (Retrieval-Augmented Generation) để giúp khách hàng tìm sản phẩm dễ dàng hơn bằng cách kết hợp:

1. **Retrieval**: Tìm kiếm sản phẩm liên quan trong database
2. **Augmentation**: Xây dựng context từ dữ liệu thực tế
3. **Generation**: Sử dụng Gemini AI để sinh câu trả lời tự nhiên

## Cài đặt

### 1. Thêm API Key

Tạo file `.env` ở thư mục root:

```
GEMINI_API_KEY=your-gemini-api-key-here
```

Lấy API key tại: https://aistudio.google.com/app/apikey

### 2. Cấu hình (application.properties)

```properties
spring.ai.gemini.api-key=${GEMINI_API_KEY}
spring.ai.gemini.chat.options.model=gemini-1.5-pro
spring.ai.gemini.chat.options.temperature=0.8
spring.ai.gemini.chat.options.max-tokens=1000
```

### 3. Dependencies

Đã được thêm trong `pom.xml`:

- `spring-boot-starter-webflux` (cho WebClient)
- `spring-dotenv` (để load .env file)

## Sử dụng

1. Chạy ứng dụng:

   ```powershell
   mvn spring-boot:run
   ```

2. Mở trình duyệt, truy cập bất kỳ trang nào

3. Click vào nút chat tròn màu xanh ở góc dưới bên phải

4. Gõ câu hỏi, ví dụ:
   - "Tôi muốn tìm sản phẩm giá rẻ"
   - "Có laptop nào không?"
   - "Gợi ý sản phẩm dưới 5 triệu"

## Cách hoạt động

```
User Question
     ↓
[Keyword Extraction]
     ↓
[Search Database] → Find relevant products
     ↓
[Build Context] → Format product info
     ↓
[Create Prompt] → Combine context + question
     ↓
[Call Gemini API] → Generate natural response
     ↓
Bot Response
```

## API Endpoints

### POST /api/chat

Request:

```json
{
  "message": "Tôi muốn tìm laptop"
}
```

Response:

```json
{
  "response": "Dựa trên thông tin trong cơ sở dữ liệu..."
}
```

## Files liên quan

- `ChatbotService.java` - Logic RAG và gọi Gemini API
- `ChatbotController.java` - REST API endpoint
- `GeminiProperties.java` - Configuration binding
- `WebClientConfig.java` - WebClient bean setup
- `layout.html` - Chat widget UI

## Troubleshooting

Xem file `CHATBOT_DEBUG.md` để biết cách debug các lỗi thường gặp.

## Giới hạn hiện tại

- Keyword matching đơn giản (chưa có semantic search)
- Không lưu lịch sử chat
- Giới hạn 5 sản phẩm trong context
- Phụ thuộc vào Gemini API (cần internet)

## Cải tiến trong tương lai

- [ ] Thêm vector embeddings cho semantic search
- [ ] Lưu conversation history
- [ ] Thêm quick replies
- [ ] Cache responses
- [ ] Rate limiting
- [ ] Multi-language support
