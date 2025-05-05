From maven:latest as builder

WORKDIR /app

COPY . .

RUN chmod +x mvnw


RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8083

ENV JAVA_OPTS="-Xms512m -Xmx1024m"

ENTRYPOINT ["/bin/sh", "-c", "java $JAVA_OPTS -jar app.jar"]
