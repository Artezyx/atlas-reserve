FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests package
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/target/atlas-reserve-0.1.0.jar app.jar
RUN addgroup -S app && adduser -S -G app app
USER app
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
