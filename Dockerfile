FROM maven:3.9.9-eclipse-temurin AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn dependency:go-offline
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app
RUN mkdir -p /usr/share/fonts/truetype/
COPY fonts/Arial.ttf /usr/share/fonts/truetype/
RUN fc-cache -f -v
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]