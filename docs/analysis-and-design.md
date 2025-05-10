# 📊 Hệ thống Microservices - Phân tích và Thiết kế

Tài liệu này phác thảo quá trình phân tích và thiết kế cho hệ thống dựa trên microservices. Sử dụng tài liệu này để giải thích tư duy và các quyết định về kiến trúc của bạn.

## 1. 🎯 Phát biểu Bài toán (Problem Statement)

*   **Mô tả bài toán:** Hệ thống này giải quyết vấn đề tổ chức các kỳ thi trắc nghiệm trực tuyến có giới hạn thời gian cho các sinh viên cụ thể. Thay vì hệ thống đăng nhập tập trung, quyền truy cập vào mỗi bài thi được kiểm soát bằng mã xác thực (access code) duy nhất cho từng sinh viên, chỉ hợp lệ trong một khung thời gian định trước.
*   **Người dùng:**
    *   **Sinh viên (Chính):** Tham gia làm bài thi bằng mã xác thực được cấp.
    *   **(Tiềm năng) Quản trị viên/Giảng viên:** Tạo bài thi, cấp quyền và mã truy cập cho sinh viên, xem kết quả (chức năng này chưa được chi tiết trong use case chính).
*   **Mục tiêu chính:**
    *   Cho phép các sinh viên được chỉ định tham gia bài thi cụ thể bằng mã xác thực trong khung giờ cho phép.
    *   Cung cấp câu hỏi thi cho sinh viên.
    *   Thu thập bộ câu trả lời cuối cùng từ sinh viên khi họ nộp bài (trạng thái câu trả lời được quản lý phía client).
    *   Tự động chấm điểm bài làm đã nộp.
    *   Lưu trữ kết quả thi.
    *   (Tùy chọn) Thông báo kết quả cho sinh viên.
*   **Loại dữ liệu xử lý:** Thông tin sinh viên (ID, tên), Nội dung bài thi (câu hỏi, lựa chọn, đáp án đúng), Thông tin truy cập bài thi (sinh viên được phép, mã xác thực, khung giờ hợp lệ, trạng thái mã), Siêu dữ liệu phiên làm bài (thời gian bắt đầu/kết thúc, trạng thái), Bộ câu trả lời do sinh viên nộp, Điểm số, Kết quả thi chi tiết, Thông báo.

## 2. 🧩 Các Microservices được Xác định

Danh sách các microservices trong hệ thống và trách nhiệm của chúng.

| Tên Dịch vụ (Service Name)   | Trách nhiệm                                                                                                                      | Tech Stack         |
|:-----------------------------|:---------------------------------------------------------------------------------------------------------------------------------|:---------------------------------------|
| `student-service`            | Quản lý thông tin cơ bản của sinh viên (ID, tên, số điện thoại, email).                                                          | Java Spring Boot                       |
| `quiz-service`               | Quản lý bài thi (câu hỏi, đáp án), quản lý quyền truy cập & mã xác thực (`QuizAccess`), xác thực mã truy cập và khung thời gian. | Java Spring Boot                       |
| `quiz-participation-service` | Xử lý quy trình tham gia thi, từ xác thực sinh viên, gửi câu hỏi, nộp bài.                                                       | Java Spring Boot                       |
| `result-service`             | Xử lý dữ liệu nộp bài, chấm điểm và lưu trữ kết quả bài thi.                                                                    | Java Spring Boot + MongoDB             |
| `notification-service`       | Gửi thông báo kết quả bài thi                                                                                                    | Java Spring Boot                       |
| `api-gateway`                | Điểm vào duy nhất cho client, định tuyến request, có thể xử lý một số vấn đề chung (rate limiting, logging cơ bản).              | Spring Cloud Gateway / Nginx           |

## 3. 🔄 Giao tiếp Dịch vụ (Service Communication)

Mô tả cách các dịch vụ tương tác:

*   **Client ⇄ API Gateway (REST API qua HTTPS):** Client gửi các yêu cầu HTTP (bắt đầu thi, nộp bài với tất cả câu trả lời, xem kết quả,...) đến Gateway.
*   **API Gateway ⇄ Các Microservice (REST API qua mạng nội bộ):** Gateway định tuyến các yêu cầu đến service phù hợp (SS, ESS, QS, RS,...).
*   **Giao tiếp Đồng bộ Service-Service (REST API):**
    *   `Quiz Participation Service` ⇄ `Quiz Service` (Để xác thực mã truy cập và lấy câu hỏi).
    *   `Result Service` ⇄ `Quiz Service` (Để lấy đáp án đúng).
    *   `Notification Service` ⇄ `Student Service` (Để lấy thông tin liên lạc).
*   **Giao tiếp Bất đồng bộ Service-Service (Kafka):**
    *   `Quiz Participation Service` → `Message Broker` (Topic: `quiz_submissions`) → `Result Service` (Để gửi bài làm đi chấm điểm, bài làm nhận trực tiếp từ client).
    *   `Result Service` → `Message Broker` (Topic: `notifications`) → `Notification Service` (Để yêu cầu gửi thông báo kết quả).

## 4. 🗂️ Thiết kế Dữ liệu (Data Design)

Dưới đây là thiết kế dữ liệu chi tiết cho từng microservice:

### `student-service`
- **Mô hình dữ liệu:**
  - Bảng `students`:
    - `id` (Primary Key): UUID
    - `name`: String
    - `email`: String (unique)
    - `phone_number`: String (optional)
    - `created_at`: Timestamp
    - `updated_at`: Timestamp

### `quiz-service`
- **Mô hình dữ liệu:**
  - Bảng `quizzes`:
    - `id` (Primary Key): UUID
    - `title`: String
    - `description`: Text
    - `valid_from`: Timestamp
    - `valid_until`: Timestamp
    - `created_at`: Timestamp
    - `updated_at`: Timestamp
  - Bảng `questions`:
    - `id` (Primary Key): UUID
    - `quiz_id` (Foreign Key): UUID
    - `content`: Text
    - `option_a`: String (đáp án A)
    - `option_b`: String (đáp án B) 
    - `option_c`: String (đáp án C)
    - `option_d`: String (đáp án D)
    - `correct_option`: Enum (`A`, `B`, `C`, `D`) (đáp án đúng)
    - `created_at`: Timestamp
    - `updated_at`: Timestamp
  - Bảng `quiz_access_codes`:
    - `id` (Primary Key): UUID
    - `quiz_id` (Foreign Key): UUID
    - `student_id` (Foreign Key): String
    - `access_code`: String (unique)
    - `valid_from`: Timestamp
    - `valid_until`: Timestamp
    - `created_at`: Timestamp
    - `updated_at`: Timestamp

### `quiz-participation-service`
- **Mô hình dữ liệu:**
  - Bảng `exam_sessions`:
    - `id` (Primary Key): UUID
    - `student_id` (Foreign Key): String
    - `quiz_id` (Foreign Key): UUID
    - `start_time`: Timestamp
    - `end_time`: Timestamp
    - `status`: Enum (`STARTED`, `SUBMITTED`, `CANCELLED`)
    - `created_at`: Timestamp
    - `updated_at`: Timestamp

### `result-service`
- **Mô hình dữ liệu (MongoDB):**
  - Collection `results`:
    - `_id`: ObjectId (tự động tạo bởi MongoDB)
    - `exam_session_id`: String (UUID của phiên thi)
    - `student_id`: String (UUID của sinh viên)
    - `quiz_id`: String (UUID của bài thi)
    - `score`: Double (điểm tổng)
    - `total_question`: Int (Số lượng câu hỏi)
    - `scoring_details`: Array (chi tiết điểm từng câu hỏi)
      - `question_id`: String
      - `selected_option`: String (A, B, C, D)
      - `correct_option`: String (A, B, C, D)
      - `is_correct`: Boolean
      - `score`: Double (điểm cho câu hỏi)
    - `submission_time`: Date (thời điểm nộp bài)
    - `status`: String (GRADED, ERROR)
    - `metadata`: Object (dữ liệu bổ sung nếu có)
    - `created_at`: Date

### `kafka message payload` 

#### Topic: `quiz_submissions`
Message này được gửi từ `quiz-participation-service` đến `result-service` khi sinh viên nộp bài thi.

```json
{
  "messageId": "uuid-message-123456",
  "timestamp": "2025-05-03T10:15:30Z",
  "eventType": "QUIZ_SUBMISSION_CREATED",
  "payload": {
    "examSessionId": "uuid-session-789012",
    "studentId": "uuid-student-345678",
    "quizId": "uuid-quiz-901234",
    "submissionTime": "2025-05-03T10:15:25Z",
    "answers": [
      {
        "questionId": "uuid-question-567890",
        "selectedOption": "B"
      },
      {
        "questionId": "uuid-question-678901",
        "selectedOption": "A"
      },
      {
        "questionId": "uuid-question-789012",
        "selectedOption": "D"
      }
      // ... các câu trả lời khác
    ],
    "metadata": {
      "clientIp": "192.168.1.100",
      "userAgent": "Mozilla/5.0..."
    }
  }
}
```

#### Topic: `notifications`
Message này được gửi từ `result-service` đến `notification-service` khi kết quả bài thi đã được tính toán xong.

```json
{
  "messageId": "uuid-message-234567",
  "timestamp": "2025-05-03T10:15:45Z",
  "eventType": "RESULT_GRADED",
  "payload": {
    "examSessionId": "uuid-session-789012",
    "studentId": "uuid-student-345678",
    "quizId": "uuid-quiz-901234",
    "score": 85.5,
    "totalQuestions": 10,
    "correctAnswers": 8,
    "gradedAt": "2025-05-03T10:15:40Z",
    "notificationType": "EMAIL",
    "templateId": "quiz-result-template",
    "priority": "NORMAL"
  }
}
```


## 5. 🔐 Cân nhắc Bảo mật (Security Considerations)

*   **Mã Truy cập (Access Code):**
    *   Quy trình tạo và phân phối mã xác thực cho sinh viên cần đảm bảo an toàn (nằm ngoài phạm vi hệ thống này).
    *   Mã phải đủ mạnh (dài, ngẫu nhiên) để khó đoán.
*   **Xác thực phía Server:**
    *   `Quiz Service` phải xác thực nghiêm ngặt sự kết hợp của `quizId`, `studentId`, `accessCode`.
    *   Kiểm tra chặt chẽ khung thời gian (`validFrom`, `validUntil`).
    *   Đảm bảo việc cập nhật trạng thái mã (`status` = `USED`) là **nguyên tử (atomic)** để tránh race condition (hai yêu cầu dùng cùng một mã).
*   **Kiểm tra Thời gian Nộp bài:** `Exam Session Service` phải xác thực `submissionTime` dựa trên `deadline` của phiên.
*   **Xác thực Đầu vào (Input Validation):** Tất cả dữ liệu nhận được từ client (qua API Gateway) và giữa các service phải được xác thực để ngăn chặn các tấn công phổ biến (injection, XSS,...).


## 6. 📦 Kế hoạch Triển khai (Deployment Plan)

### 6.1 Chiến lược Containerization


### 6.3 Cấu hình Môi trường

* **Quản lý Biến Môi trường**: Sử dụng file `.env` riêng cho mỗi môi trường (dev, staging, production):
  ```
  # .env.example
  # MySQL
  MYSQL_ROOT_PASSWORD=root_password
  MYSQL_DATABASE=quiz_db
  MYSQL_USER=quiz_user
  MYSQL_PASSWORD=quiz_password
  
  # MongoDB
  MONGO_USERNAME=result_user
  MONGO_PASSWORD=result_password
  MONGO_DATABASE=result_db
  
  # SMTP
  SMTP_HOST=smtp.example.com
  SMTP_PORT=587
  SMTP_USERNAME=notification@example.com
  SMTP_PASSWORD=notification_password
  ```

* **Externalizing Configuration**: Sử dụng Spring Cloud Config (hoặc Kubernetes ConfigMaps/Secrets trong production) để quản lý cấu hình ứng dụng một cách tập trung.

### 6.4 CI/CD Pipeline

* **Continuous Integration**:
  * Sử dụng GitHub Actions hoặc Jenkins cho việc build và test tự động.
  * Chạy kiểm thử đơn vị (Unit Tests) và kiểm thử tích hợp (Integration Tests).
  * Quét mã nguồn với SonarQube để đảm bảo chất lượng code.

* **Continuous Deployment**:
  * Môi trường Development: Tự động deploy khi có commit vào nhánh `develop`.
  * Môi trường Staging: Deploy thủ công hoặc tự động khi có tag release candidate.
  * Môi trường Production: Deploy thủ công sau khi đã kiểm thử đầy đủ ở Staging.

### 6.5 Môi trường Production với Kubernetes

* **Kubernetes Cluster**: Triển khai trên AWS EKS, GCP GKE hoặc Azure AKS.

* **Tài nguyên Kubernetes**:
  * Deployments: Quản lý các Pods của từng microservice.
  * Services: Cung cấp điểm truy cập ổn định cho mỗi microservice.
  * Ingress: Quản lý các request từ bên ngoài vào cluster.
  * ConfigMaps/Secrets: Quản lý cấu hình và thông tin nhạy cảm.
  * StatefulSets: Cho các dịch vụ cần lưu trạng thái (Kafka, MongoDB, MySQL).

### 6.6 Monitoring, Logging và Backup

* **Monitoring**: 
  * Sử dụng Prometheus để thu thập metrics từ các service thông qua Spring Boot Actuator.
  * Grafana để hiển thị dashboard và thiết lập cảnh báo.

* **Logging**: 
  * Centralized logging với ELK Stack (Elasticsearch, Logstash, Kibana) hoặc Graylog.
  * Cấu hình log rotation để tối ưu dung lượng.

* **Backup & Recovery**:
  * Backup định kỳ cho MySQL và MongoDB theo lịch trình (ít nhất hàng ngày).
  * Lưu trữ backup ở vị trí khác với production để phòng thảm họa.
  * Thực hiện định kỳ bài tập phục hồi dữ liệu để đảm bảo tính hiệu quả của chiến lược backup.

### 6.7 Quy trình Rollout & Rollback

* **Blue-Green Deployment**: 
  * Duy trì hai môi trường production song song (Blue và Green).
  * Triển khai phiên bản mới trên môi trường không active.
  * Chuyển đổi traffic sang môi trường mới sau khi kiểm thử.

* **Canary Releases**:
  * Triển khai phiên bản mới cho một tỷ lệ nhỏ người dùng.
  * Mở rộng dần khi xác nhận phiên bản mới hoạt động ổn định.

* **Rollback Strategy**: 
  * Khả năng rollback nhanh chóng về phiên bản trước đó trong trường hợp phát hiện lỗi.
  * Sử dụng Kubernetes history và version tagging để quản lý phiên bản.

## 7. 🎨 Sơ đồ Kiến trúc (Architecture Diagram)

*(Sơ đồ ASCII đơn giản - Nên thay thế bằng hình ảnh chi tiết hơn)*

