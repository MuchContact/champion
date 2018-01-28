FROM java:8-jre
COPY target/champion-0.0.1-SNAPSHOT.jar /usr/src/champion/
WORKDIR /usr/src/champion
EXPOSE 8080
CMD ["java", "-jar", "champion-0.0.1-SNAPSHOT.jar"]