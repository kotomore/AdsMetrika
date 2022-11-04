FROM openjdk:19
ARG JAR_FILE=/target/*.jar
COPY ${JAR_FILE} AdsMetrika.jar
ENTRYPOINT ["java","-jar","/AdsMetrika.jar"]