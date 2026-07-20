# syntax=docker/dockerfile:1.7

ARG MAVEN_IMAGE=maven:3.9.9-eclipse-temurin-21
ARG RUNTIME_IMAGE=eclipse-temurin:21-jre-alpine
ARG MODULE_NAME

FROM ${MAVEN_IMAGE} AS dependencies
WORKDIR /workspace

ARG MODULE_NAME

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY discovery-server/pom.xml discovery-server/pom.xml
COPY lifebalance-common/pom.xml lifebalance-common/pom.xml
COPY lifebalance-security/pom.xml lifebalance-security/pom.xml
COPY gateway/pom.xml gateway/pom.xml
COPY identity-service/pom.xml identity-service/pom.xml
COPY task-service/pom.xml task-service/pom.xml
COPY timeline-service/pom.xml timeline-service/pom.xml
COPY finance-service/pom.xml finance-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
COPY analytics-service/pom.xml analytics-service/pom.xml
COPY ai-service/pom.xml ai-service/pom.xml

RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && \
    ./mvnw -B -q -pl ${MODULE_NAME} -am dependency:go-offline -DskipTests

FROM dependencies AS build
WORKDIR /workspace

ARG MODULE_NAME

COPY . .

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -q -pl ${MODULE_NAME} -am clean package -DskipTests

FROM ${RUNTIME_IMAGE} AS runtime
WORKDIR /app

RUN addgroup -S lifebalance && \
    adduser -S lifebalance -G lifebalance && \
    apk add --no-cache curl tzdata

ARG MODULE_NAME

COPY --from=build /workspace/${MODULE_NAME}/target/*.jar /app/app.jar

USER lifebalance:lifebalance

EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport" \
    SERVER_PORT=8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
