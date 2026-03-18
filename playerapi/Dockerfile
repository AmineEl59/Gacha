# Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/playerapi-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-Dspring.data.mongodb.host=mongodb", "-Dspring.data.mongodb.port=27017", "-Dspring.data.mongodb.database=playerapi", "-jar", "app.jar"]
