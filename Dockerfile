
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app


COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-alpine AS runtime

WORKDIR /app

COPY --from=build /app/target/surveyapp.jar .

CMD ["java", "-jar", "surveyapp.jar"]
