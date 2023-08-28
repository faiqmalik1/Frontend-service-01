FROM openjdk:17
VOLUME /tmp
EXPOSE 3000
ARG JAR_FILE=target/frontend-service-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]