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

### Docker deployment
1. **Clone repository**

   ```bash
   git clone https://github.com/jnp2018/mid-project-580736435.git
   cd mid-project-580736435
   ```


2. **Khởi chạy hệ thống bằng Docker Compose**

   ```bash
   docker-compose -f docker-compose.yml up --build
   ```
---

### Kubernetes deployment (kind cluster)

1. **Start up external docker infrastructure**
   ```bash
   docker-compose -f docker-compose-infra.yml up --build
   ```

2. **Start up kubernetes cluster (kind)**
   ```bash
   kind create cluster --config ./k8s/kind-config.yaml
   ```
3. **Deploy kubernetes resources**
   ```bash
   kubectl apply -f ./k8s/gateway-k8s-deploy.yml 
   kubectl apply -f ./k8s/quiz-participation-service-k8s-deploy.yml
   kubectl apply -f ./k8s/quiz-service-k8s-deploy.yml
   kubectl apply -f ./k8s/result-service-k8s-deploy.yml
   ```

