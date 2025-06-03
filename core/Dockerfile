FROM gradle:7.6-jdk21 AS builder
WORKDIR /home/gradle/project

COPY gradlew gradlew
COPY gradle gradle

COPY . .

RUN chmod +x gradlew && \
    ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

VOLUME /tmp

COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
