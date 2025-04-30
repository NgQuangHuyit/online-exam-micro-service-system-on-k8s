#  Online Quiz System - Microservice Architecture (SOA-based)

## 1.  Problem Statement

###  Description of the problem
Hệ thống thi trắc nghiệm trực tuyến cho phép sinh viên làm bài thi online thông qua việc xác thực thông tin cá nhân, nhận câu hỏi, nộp bài trong thời gian giới hạn, chấm điểm và lưu kết quả.

### Subjects of use
- Sinh viên (Người dùng cuối)
- Quản trị viên hệ thống (để theo dõi kết quả, không trực tiếp tương tác trong luồng chính)

###  Main goal
- Cung cấp giao diện làm bài trắc nghiệm nhanh chóng, chính xác.
- Tự động xác thực, phân phối câu hỏi và tính điểm.
- Ghi nhận kết quả để phục vụ thống kê, phân tích.

### Processing data
- Thông tin sinh viên
- Danh sách câu hỏi, đáp án đúng
- Câu trả lời của sinh viên
- Thời gian làm bài
- Kết quả và điểm số

---

## 2.  Identified Microservices

| Service Name             | Responsibility                                      | Tech Stack        |
|--------------------------|------------------------------------------------------|-------------------|
| `auth-service`           | Xử lý xác thực sinh viên                            | Python Flask      |
| `question-service`       | Cung cấp danh sách câu hỏi trắc nghiệm              | Python Flask      |
| `answer-service`         | Quản lý đáp án đúng và chấm điểm câu trả lời        | Python Flask      |
| `score-service`          | Ghi nhận và truy xuất kết quả thi                   | Python Flask      |
| `submission-service`     | Kiểm tra thời gian nộp bài và hợp lệ hóa bài nộp    | Python Flask      |
| `notification-service`   | Gửi thông báo lỗi xác thực hoặc nộp trễ             | Python Flask / SMTP |
| `quiz-participation`     | Điều phối toàn bộ luồng làm bài thi trắc nghiệm     | Python Flask      |
| `gateway`                | Cổng giao tiếp giữa frontend và các service backend | Nginx / Flask     |

---

## 3.  Service Communication

- `Gateway` ⇄ `auth-service`: xác thực sinh viên qua REST
- `Gateway` ⇄ `quiz-participation`: điều phối bài làm
- `quiz-participation` ⇄ `question-service`: lấy câu hỏi
- `quiz-participation` ⇄ `submission-service`: xác minh thời gian
- `quiz-participation` ⇄ `answer-service`: lấy đáp án và tính điểm
- `quiz-participation` ⇄ `score-service`: lưu kết quả
- `quiz-participation` ⇄ `notification-service`: gửi cảnh báo lỗi

Tất cả giao tiếp thực hiện qua **REST API** với định dạng JSON.

---

## 4.  Data Design

#### a. `Students`
Chứa thông tin sinh viên: mã số, tên, email,... Dùng để xác thực và lưu kết quả thi.

#### b. `Quiz`
Đại diện cho mỗi bài thi cụ thể, bao gồm tên, thời gian làm bài, thời điểm bắt đầu và kết thúc.

#### c. `Questions`
Chứa danh sách câu hỏi. Một câu hỏi có thể thuộc nhiều bài thi khác nhau.

#### d. `QuizQuestions`
Bảng trung gian giữa `Quiz` và `Questions`, thể hiện mối quan hệ nhiều-nhiều.

#### e. `Answers`
Chứa đáp án đúng cho từng câu hỏi. Giúp hệ thống chấm điểm tự động.

#### f. `StudentAnswers`
Lưu câu trả lời mà sinh viên đã chọn, kèm thời gian nộp và trạng thái (có đúng hay không, có trễ hạn không).

#### g. `Scores`
Lưu tổng điểm cuối cùng và thời gian hoàn thành bài của từng sinh viên.
###  Quan hệ giữa các bảng
- `Students` ↔ `StudentAnswers`: 1-n
- `Questions` ↔ `Answers`: 1-1
- `Quiz` ↔ `QuizQuestions`: 1-n
- `QuizQuestions` ↔ `Questions`: n-1
- `StudentAnswers` ↔ `Questions`: n-1
- `Students` ↔ `Scores`: 1-1

###  ER Diagram Suggestion
![Entity Relationship Diagram](https://imgur.com/dxhQ7sM)

---

## 5.  Security Considerations

-  Dùng **JWT (JSON Web Token)** để quản lý phiên đăng nhập.
-  Tất cả input đều được kiểm tra và sanitize ở từng service.
-  API sử dụng phân quyền dựa trên vai trò (role-based access).

---

## 6.  Deployment Plan

- Dùng **Docker Compose** để dựng môi trường local với các service độc lập.
- Mỗi microservice có Dockerfile riêng.
- Cấu hình biến môi trường lưu trong `.env`.
- Giao tiếp qua bridge network nội bộ của Docker.

```yaml
version: "3.8"
services:
  auth-service:
    build: ./auth
    ports: [5001:5000]
  question-service:
    build: ./question
  answer-service:
    build: ./answer
  score-service:
    build: ./score
  submission-service:
    build: ./submission
  quiz-participation:
    build: ./quiz
  notification-service:
    build: ./notify
  gateway:
    build: ./gateway
    ports: [80:80]
```

---

## 7.  Architecture Diagram
![Biểu đồ UC tổng quan ](https://imgur.com/95ls8Ja)

![Sequence Diagram](https://www.mermaidchart.com/raw/1ced8096-6705-4402-87aa-a7992d78e2db?theme=light&version=v0.1&format=svg)
## 7.1  Sinh viên truy cập và xác thực
Student → Gateway: Gửi thông tin đăng nhập (tên/mã sinh viên)
Gateway → Auth Service: Kiểm tra xác thực
Auth Service → Gateway: Trả kết quả xác thực (thành công/thất bại)

[THẤT BẠI]
Gateway → Notification Service: Gửi thông báo lỗi
Notification Service → Student: Hiển thị lỗi xác thực




## 7.2 Lấy câu hỏi và bắt đầu làm bài


Student → Gateway: Bắt đầu làm bài
Gateway → Quiz Participation Service: Khởi tạo bài thi
Quiz Participation → Question Service: Lấy câu hỏi
Question Service → Quiz Participation: Trả câu hỏi
Quiz Participation → Gateway → Student: Hiển thị câu hỏi


## 7.3  Sinh viên trả lời và nộp bài

Student → Gateway: Gửi câu trả lời
Gateway → Quiz Participation: Truyền câu trả lời
Quiz Participation → Submission Service: Kiểm tra thời gian nộp bài

[QUÁ HẠN]
Submission Service → Quiz Participation
Quiz Participation → Notification Service
Notification Service → Student

[HỢP LỆ]
Quiz Participation → Answer Service: So sánh với đáp án
Answer Service → Quiz Participation: Trả điểm
Quiz Participation → Score Service: Lưu kết quả


