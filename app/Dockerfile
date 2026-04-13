FROM eclipse-temurin:21.0.6_7-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY build/libs/*-SNAPSHOT.jar app.jar
COPY build/libs/lib/ lib/
EXPOSE 8080 8081
USER appuser
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:InitialRAMPercentage=50.0", "-XX:+UseContainerSupport", "-cp", "app.jar:lib/*", "miotaxi.aidemo.OtusApplicationKt"]
