# =========================
# Etapa 1: Build
# =========================
FROM gradle:9.8.2-jdk21 AS build

WORKDIR /app

COPY . .

RUN chmod +x gradlew

RUN ./gradlew clean test bootJar --no-daemon


# =========================
# Etapa 2: Runtime
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiar el JAR
COPY --from=build /app/build/libs/*.jar app.jar

# Copiar la wallet Oracle
# la wallet no va en GitHub: ponla en la carpeta Wallet_ESFOCAR solo al construir la imagen
COPY --from=build /app/Wallet_ESFOCAR ./Wallet_ESFOCAR

# Debug: mostrar archivos copiados
RUN echo "=== CONTENIDO DE /app ===" && ls -la /app

RUN echo "=== CONTENIDO DE /app/Wallet_ESFOCAR ===" && ls -la /app/Wallet_ESFOCAR

EXPOSE 10000

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-10000} -jar app.jar"]