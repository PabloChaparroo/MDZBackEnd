# Usar JDK 21 como imagen base
FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu

# Establecer el directorio de trabajo en el contenedor
WORKDIR /app

# Copiar el archivo pom.xml y descargar dependencias (para aprovechar cache de Docker)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Dar permisos de ejecución al wrapper de Maven
RUN chmod +x ./mvnw

# Descargar dependencias
RUN ./mvnw dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Construir la aplicación
RUN ./mvnw clean package -DskipTests

# Exponer el puerto de la app (coincide con server.port en application.properties)
EXPOSE 8081

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "target/carpinteria-0.0.1-SNAPSHOT.jar"]
