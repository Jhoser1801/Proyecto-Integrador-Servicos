# ---- etapa 1: compilar el .jar ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
# gradlew puede venir con saltos de linea de Windows (CRLF): se normaliza para que Linux lo ejecute
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew bootJar --no-daemon -x test

# ---- etapa 2: ejecutar ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
# la base H2 queda en /app/data; se guarda en un volumen para que sobreviva al contenedor
VOLUME /app/data
EXPOSE 8080 7070
ENTRYPOINT ["java", "-jar", "app.jar"]
