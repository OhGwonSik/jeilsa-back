# syntax=docker/dockerfile:1

# Gradle 빌드라고 알려주기
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle* ./

RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

RUN BOOT_JAR="$(ls build/libs/*.jar | grep -Ev 'plain|sources|javadoc' | head -n 1)" \
 && echo "Using boot jar: ${BOOT_JAR}" \
 && cp "${BOOT_JAR}" /workspace/app.jar

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /workspace/app.jar /app/app.jar

EXPOSE 8080

# 빌드 시 profile 전달
ARG SPRING_PROFILE=prod
ENV SPRING_PROFILE=${SPRING_PROFILE}
ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT sh -c "java \$JAVA_OPTS -jar /app/app.jar --spring.profiles.active=\$SPRING_PROFILE"