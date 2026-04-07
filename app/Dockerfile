FROM eclipse-temurin:21.0.6_7-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY build/libs/app-*SNAPSHOT.jar app.jar
EXPOSE 8080 8081
USER appuser
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:InitialRAMPercentage=50.0", "-XX:+UseContainerSupport", "-jar", "app.jar"]
