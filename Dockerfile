FROM maven:3.6.3-jdk-11 as buildStage

COPY . /app
WORKDIR /app

RUN mvn clean package

FROM openjdk:11-jre-slim

ENV SPRING_PROFILES_ACTIVE=docker
COPY --from=buildStage /app/target/icode-be-0.0.1-SNAPSHOT.jar /app/icode-be.jar

WORKDIR /app
EXPOSE 8080
VOLUME [ "/data/icode" ]

ENTRYPOINT [ "java", "-jar", "icode-be.jar" ]