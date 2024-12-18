FROM maven:3.9.9-eclipse-temurin AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn dependency:go-offline
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Use apk for Alpine Linux
RUN apk add --no-cache curl

ENV OLLAMA_HOST=ray000.ddns.net \
    OLLAMA_PORT=11434 \
    SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod

ARG JWT_SECRET=chat-room-validate
ENV JWT_SECRET=${JWT_SECRET}

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]