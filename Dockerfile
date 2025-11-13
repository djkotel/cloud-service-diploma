FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/cloud-service-1.0.0.jar app.jar

RUN mkdir -p /app/storage

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
