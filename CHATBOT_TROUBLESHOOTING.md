# Hướng dẫn Test và Sửa Lỗi Chatbot

## Những thay đổi mới nhất

1. **Cải thiện error handling** trong ChatbotService:

   - Thêm WebClientResponseException handler riêng
   - Hiển thị HTTP status code và response body khi có lỗi
   - Thêm debug log cho request body
   - Thêm fallback response khi API fails

2. **Thêm test endpoint**: GET `/api/chat/test`
   - Kiểm tra xem API key có được load không
   - Xem 10 ký tự đầu của API key
   - Xem model name và temperature

## Bước 1: Test API Key Configuration

Trước tiên, hãy kiểm tra xem API key có được load đúng không:

1. **Chạy ứng dụng** (nếu chưa chạy):

   ```powershell
   mvn spring-boot:run
   ```

2. **Mở trình duyệt** và truy cập:

   ```
   http://localhost:8081/api/chat/test
   ```

3. **Kiểm tra response**:
   ```json
   {
     "apiKeyConfigured": true,
     "apiKeyPrefix": "AIzaSyAUi",
     "model": "gemini-2.5-flash",
     "temperature": 0.8
   }
   ```

**Nếu `apiKeyConfigured` là `false`**:

- API key chưa được load
- Kiểm tra file `.env` có tồn tại không
- Restart ứng dụng

**Nếu `apiKeyConfigured` là `true`**:

- API key đã được load
- Tiếp tục bước 2

## Bước 2: Test Chat Trực Tiếp

Gửi request test đơn giản:

```powershell
# PowerShell
$body = @{
    message = "Xin chào"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8081/api/chat" `
    -Method Post `
    -Body $body `
    -ContentType "application/json"
```

Hoặc dùng curl:

```bash
curl -X POST http://localhost:8081/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Xin chào"}'
```

## Bước 3: Xem Logs trong Console

Khi gửi tin nhắn chat, trong console bạn sẽ thấy:

```
DEBUG - API Key: AIzaSyAUi...
DEBUG - Model: gemini-1.5-pro
DEBUG - Calling Gemini API...
DEBUG - Request body: {contents=[{parts=[{text=...}]}]}
DEBUG - Response received: [candidates, usageMetadata]
DEBUG - Success! Response length: 123
```

## Các Lỗi Thường Gặp và Cách Sửa

### Lỗi 1: "API key not valid"

**Logs sẽ hiển thị**:

```
HTTP Error: 400 - {"error": {"code": 400, "message": "API key not valid..."}}
```

**Cách sửa**:

1. API key không đúng hoặc đã hết hạn
2. Tạo API key mới: https://aistudio.google.com/app/apikey
3. Cập nhật trong `.env`:
   ```
   GEMINI_API_KEY=your-new-api-key-here
   ```
4. Restart ứng dụng

### Lỗi 2: "Model not found" hoặc 404

**Logs sẽ hiển thị**:

```
HTTP Error: 404 - {"error": {"code": 404, "message": "models/... not found"}}
```

**Cách sửa**:
Thay đổi model trong `application.properties`:

```properties
# Thử các model sau:
spring.ai.gemini.chat.options.model=gemini-1.5-flash
# Hoặc
spring.ai.gemini.chat.options.model=gemini-pro
```

### Lỗi 3: "Quota exceeded" hoặc 429

**Logs sẽ hiển thị**:

```
HTTP Error: 429 - {"error": {"code": 429, "message": "Resource exhausted..."}}
```

**Cách sửa**:

- Đợi 1-2 phút rồi thử lại
- Hoặc tạo project mới và API key mới

### Lỗi 4: Connection timeout

**Logs sẽ hiển thị**:

```
ERROR calling Gemini API: TimeoutException - ...
```

**Cách sửa**:

- Kiểm tra kết nối internet
- Kiểm tra firewall có chặn không
- Thử lại sau

### Lỗi 5: NullPointerException hoặc ClassCastException

**Logs sẽ hiển thị**:

```
ERROR calling Gemini API: NullPointerException - ...
```

**Cách sửa**:

- Response format từ Gemini đã thay đổi
- Copy full response từ log: "DEBUG - Response received: ..."
- Gửi cho tôi để điều chỉnh parsing code

## Fallback Mode

Nếu Gemini API không hoạt động, chatbot sẽ tự động chuyển sang fallback mode và trả về danh sách sản phẩm đơn giản:

```
Dựa trên tìm kiếm, tôi tìm thấy các sản phẩm sau:

• Laptop Dell XPS 13 - Giá: 25000000 VNĐ (Còn hàng)
• MacBook Pro M1 - Giá: 35000000 VNĐ (Còn hàng)

Bạn có thể xem chi tiết tại trang sản phẩm!
```

## Debug Checklist

- [ ] API key có trong file `.env`?
- [ ] Chạy `/api/chat/test` - `apiKeyConfigured` = true?
- [ ] Console logs có hiển thị "DEBUG - API Key: ..."?
- [ ] Console logs có "DEBUG - Calling Gemini API..."?
- [ ] Console logs có HTTP Error không? Nếu có, status code là gì?
- [ ] Có thử đổi model name chưa?
- [ ] Có test với curl/Postman chưa?

## Nếu Vẫn Lỗi

Gửi cho tôi:

1. Screenshot của response từ `/api/chat/test`
2. Full console logs từ khi gửi tin nhắn
3. Nội dung tin nhắn bạn đã gửi

Tôi sẽ phân tích và sửa chính xác!
