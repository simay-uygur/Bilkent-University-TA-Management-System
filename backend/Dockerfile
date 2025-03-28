FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# JAR yerine direkt kaynak kodunu kopyala
COPY . /app

# Geliştirme için Maven wrapper kullan (ya da gradle)
RUN ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["./mvnw", "spring-boot:run"]
