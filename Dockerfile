# ---------- Etapa 1: compilar ----------
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /build

# Dependencias primero (capa cacheable mientras no cambie el pom)
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q -DskipTests package && cp target/carpinteria-*.jar app.jar

# ---------- Etapa 2: ejecutar (solo JRE) ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Usuario sin privilegios
RUN useradd --system --uid 10001 mdz
USER mdz

COPY --from=build /build/app.jar app.jar

# Pensado para 512 MB (plan gratis de Render): heap acotado y GC simple.
# Se puede pisar con la variable JAVA_OPTS sin rebuild.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1"

# Render inyecta PORT; en local la app usa 9091 (server.port=${PORT:9091})
EXPOSE 9091

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
