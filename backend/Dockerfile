FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw package -DskipTests --no-transfer-progress

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p uploads/profile-pictures
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx400m", "-jar", "app.jar", "--spring.profiles.active=prod"]