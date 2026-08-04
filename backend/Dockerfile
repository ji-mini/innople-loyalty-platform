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
# JVM/OS 기본 시간대를 KST로 고정한다(코드의 명시적 KST 처리와 병행하는 안전망).
ENV TZ=Asia/Seoul

ENTRYPOINT ["java","-Duser.timezone=Asia/Seoul","-jar","/app/app.jar"]
