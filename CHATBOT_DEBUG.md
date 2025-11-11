# Hướng dẫn Debug Chatbot

## Những thay đổi đã thực hiện

1. **Sửa ChatbotService.java**:

   - Thêm logging chi tiết để debug
   - Sửa error handling để hiển thị lỗi cụ thể
   - Thêm @SuppressWarnings để loại bỏ warning compile
   - Kiểm tra error response từ Gemini API

2. **Sửa application.properties**:
   - Đổi model từ `gemini-2.5-flash` → `gemini-1.5-pro` (model name chính xác)

## Cách kiểm tra lỗi

### 1. Kiểm tra logs trong console

Khi chạy ứng dụng và gửi tin nhắn chat, hãy xem console output:

- `DEBUG - API Key: AIzaSyAUi...` - API key có được load không?
- `DEBUG - Model: gemini-1.5-pro` - Model name đúng chưa?
- `DEBUG - Calling Gemini API...` - Có gọi API không?
- `DEBUG - Response received: [...]` - Response có về không?
- Nếu có error: `Gemini API Error: ...` - Lỗi gì từ API?

### 2. Các lỗi thường gặp và cách sửa

#### Lỗi: "API key not valid"

**Nguyên nhân**: API key trong .env không hợp lệ hoặc đã hết hạn
**Cách sửa**:

- Tạo API key mới tại: https://aistudio.google.com/app/apikey
- Cập nhật trong file `.env`:
  ```
  GEMINI_API_KEY=your-new-api-key-here
  ```
- Restart ứng dụng

#### Lỗi: "Model not found"

**Nguyên nhân**: Model name không đúng
**Cách sửa**: Thử các model names sau trong `application.properties`:

- `gemini-1.5-pro` (recommended)
- `gemini-1.5-flash`
- `gemini-pro`

#### Lỗi: "Quota exceeded" hoặc "Rate limit"

**Nguyên nhân**: Vượt quá giới hạn free tier
**Cách sửa**:

- Đợi 1 phút rồi thử lại
- Hoặc nâng cấp lên paid plan
- Hoặc tạo project mới với API key mới

#### Lỗi: "Unable to parse Gemini response"

**Nguyên nhân**: Response format thay đổi hoặc không có candidates
**Cách sửa**: Xem log response và điều chỉnh parsing code

### 3. Test API key trực tiếp

Chạy lệnh curl để test API key:

```powershell
$apiKey = "YOUR_API_KEY_HERE"
$model = "gemini-1.5-pro"
$body = @{
  contents = @(
    @{
      parts = @(
        @{ text = "Hello" }
      )
    }
  )
} | ConvertTo-Json -Depth 10

Invoke-RestMethod -Uri "https://generativelanguage.googleapis.com/v1beta/models/$model`:generateContent?key=$apiKey" `
  -Method Post `
  -Body $body `
  -ContentType "application/json"
```

Nếu curl thành công thì API key OK, nếu lỗi thì có vấn đề với key/model.

### 4. Rebuild và restart

Sau khi sửa code:

```powershell
# Stop ứng dụng hiện tại (Ctrl+C)

# Clean và rebuild
mvn clean package -DskipTests

# Chạy lại
mvn spring-boot:run
```

### 5. Kiểm tra biến môi trường

Đảm bảo file `.env` ở đúng thư mục root của project và dependency `spring-dotenv` đã được thêm trong `pom.xml`.

## Các model Gemini có sẵn (tính đến Nov 2025)

- `gemini-1.5-pro` - Model mạnh nhất, phù hợp cho tác vụ phức tạp
- `gemini-1.5-flash` - Nhanh hơn, rẻ hơn, vẫn chất lượng tốt
- `gemini-pro` - Legacy model, vẫn dùng được

## Contact

Nếu vẫn gặp lỗi, gửi log console đầy đủ để được hỗ trợ.
