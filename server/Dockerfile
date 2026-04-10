FROM gradle:8.14.3-jdk21 AS build
WORKDIR /app

COPY . .
RUN gradle :server:buildFatJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/server/build/libs/server-all.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
