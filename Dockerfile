
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/ticket-booking-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8686

ENTRYPOINT ["java", "-jar", "app.jar"]
