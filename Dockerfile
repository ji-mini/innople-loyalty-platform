FROM gradle:8.7-jdk21 AS build
WORKDIR /home/gradle/project

COPY build.gradle settings.gradle ./
COPY src ./src

RUN gradle --no-daemon clean bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /home/gradle/project/build/libs/*.jar /app/app.jar
COPY docker/entrypoint.sh /app/entrypoint.sh

RUN chmod +x /app/entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["/app/entrypoint.sh"]

