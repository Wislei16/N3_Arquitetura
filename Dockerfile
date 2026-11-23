# Estagio 1: Compilacao e empacotamento via Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# Estagio 2: Imagem enxuta de execucao com JRE 17
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN mkdir -p /app/data
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

# Os parametros de limite de Heap (-XX:MaxRAMPercentage) e GC (-XX:+UseG1GC)
# sao injetados via JAVA_TOOL_OPTIONS conforme cada perfil de hardware.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
