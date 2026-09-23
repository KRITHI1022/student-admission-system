FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache netcat-openbsd

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "echo '=== TESTING AIVEN CONNECTION ===' && nc -vz student-admission-mysql-student-admission-db.k.aivencloud.com 18026; echo '=== STARTING SPRING BOOT ==='; exec java -jar app.jar"]