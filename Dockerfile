FROM openjdk:17-jdk-slim

RUN adduser --system spring

WORKDIR /app

COPY build/libs/ms-administracion-0.0.1-SNAPSHOT.jar app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
