FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY gradlew settings.gradle.kts ./
COPY gradle ./gradle
COPY backend/build.gradle.kts backend/
RUN chmod +x gradlew && ./gradlew --no-daemon -q :backend:dependencies > /dev/null
COPY backend/src backend/src
RUN ./gradlew --no-daemon :backend:bootJar -x test

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /src/backend/build/libs/task-tracker.jar app.jar
USER app

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -XX:TieredStopAtLevel=1" \
    PORT=8080
EXPOSE 8080 8081

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
