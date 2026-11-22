FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
ENV MAVEN_OPTS="-Xmx768m -XX:MaxMetaspaceSize=256m"
RUN mvn -B package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/quarkus-app/ ./
EXPOSE 8080
ENV TZ=America/Bogota
ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]
