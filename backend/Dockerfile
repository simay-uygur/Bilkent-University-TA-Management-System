FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY . .

RUN ./mvnw clean package -DskipTests

CMD ["java", "--enable-preview", "-jar", "target/demo1-0.0.1-SNAPSHOT.jar"]


