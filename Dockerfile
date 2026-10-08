# 1. Compilación con Maven
FROM maven:3.8.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .

# Buscar y compilar el pom.xml activo
RUN find . -name "pom.xml" -exec mvn clean package -DskipTests -f {} \;

# 2. Servidor Tomcat 9
FROM tomcat:9.0-jdk17-corretto

# Eliminar todas las aplicaciones por defecto de Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copiar el ejecutable .war generado y forzar su renombrado a ROOT.war
COPY --from=build /app/**/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]