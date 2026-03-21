# Fase 1: Build (Usamos Maven con JDK 21)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
# Copiamos solo el pom para aprovechar el cache de capas de Docker
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el código fuente y buildeamos el JAR saltando los tests (ya los corre el CI)
COPY src ./src
RUN mvn clean package -DskipTests

# Fase 2: Run (Imagen liviana con el JRE 21)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Creamos un usuario no-root por seguridad (Práctica Senior)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiamos el JAR generado en la fase anterior
COPY --from=build /app/target/*.jar app.jar

# Puerto de la app
EXPOSE 8080

# Configuración para optimizar hilos en contenedores
ENTRYPOINT ["java", "-jar", "app.jar"]
