FROM openjdk:8-jdk-alpine

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} hris-backend.jar

CMD ["java", "-jar", "/hris-backend.jar"]