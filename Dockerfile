# Bước 1: Dùng Maven và Eclipse Temurin (Java 17 chuẩn) để build project
FROM maven:3.8.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Bước 2: Chạy file .jar với môi trường Java nhẹ và ổn định nhất
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Cấu hình cổng cho Render và chạy ứng dụng
EXPOSE 10000
ENTRYPOINT ["java","-jar","app.jar"]