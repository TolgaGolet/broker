FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw \
  && ./mvnw -B -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -B -DskipTests package \
  && JAR_FILE=$(ls target/*.jar | grep -v original | head -n 1) \
  && cp "$JAR_FILE" app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd -u 1001 spring

COPY --from=builder /workspace/app.jar /app/app.jar

EXPOSE 8080

USER spring

ENTRYPOINT ["java","-jar","/app/app.jar"]


