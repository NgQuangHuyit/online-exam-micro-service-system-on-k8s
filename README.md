# 🧩 Hệ thống thi trắc nghiệm trực tuyến - Use Case: Tham gia làm bài thi trắc nghiệm


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

