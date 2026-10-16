FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --uid 10001 --create-home appuser
COPY --from=build /workspace/target/ai-pr-reviewer-0.0.1-SNAPSHOT.war /app/app.war

USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.war"]
