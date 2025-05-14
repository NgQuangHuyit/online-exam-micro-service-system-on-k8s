# 🧩 Hệ thống thi trắc nghiệm trực tuyến - Use Case: Tham gia làm bài thi trắc nghiệm

## 👥 Danh sách thành viên và phần việc đã thực hiện

| Tên sinh viên         | Mã sinh viên | Công việc thực hiện                                                                                                                                                                                                              |
|------------------------|--------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Nguyễn Quang Huy       | B21DCCN435   | - Thiết kế kiến trúc hệ thống  <br> - Xây dựng `quiz-participation-service`  <br> - Xây dựng `result-service`  <br> - Triển khai hệ thống bằng Docker Compose / Kubernetes  <br> - Hoàn thiện tài liệu phân tích & thiết kế     |
| Nguyễn Thị Phấn        | B21DCCN580   | - Xây dựng `quiz-service`  <br> - Phân tích và xác định luồng nghiệp vụ, các microservice                                                                                                                                        |
| Tống Quang Trung       | B21DCCN736   | - Xây dựng và cấu hình `API Gateway`  <br> - Xây dựng giao diện `Frontend`  <br> - Viết tài liệu API specs                                                                                                                           |

---

# 🏗️ Overall Architecture

![deployment.drawio.png](docs/asset/deployment.drawio.png)

## 🚀 Getting Started

1. **Clone this repository**

   ```bash
   git clone https://github.com/hungdn1701/microservices-assignment-starter.git
   cd mid-project-580736435
   ```

2. **Copy environment file**

   ```bash
   cp .env.example .env
   ```

3. **Run with Docker Compose**

   ```bash
   docker-compose up --build
   ```
---

