FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle ./
COPY build.gradle ./

RUN chmod +x ./gradlew

COPY src ./src

RUN ./gradlew bootJar -x test


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar ./app.jar

EXPOSE 3201

ENV SERVER_PORT=3201

ENTRYPOINT ["java","-jar","/app/app.jar"]
