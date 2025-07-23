# Start from an official OpenJDK image
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw mvnw
COPY pom.xml pom.xml
COPY .mvn .mvn

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Create final image
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/server-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
