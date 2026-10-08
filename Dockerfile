# 1. Compilar con Maven
FROM maven:3.8.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .

# Buscar y compilar el pom.xml activo en cualquier subcarpeta
RUN find . -name "pom.xml" -exec mvn clean package -DskipTests -f {} \;

# 2. Desplegar en Tomcat 9
FROM tomcat:9.0-jdk17-corretto
# Limpiar aplicaciones por defecto de Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copiar el .war generado y renombrarlo a ROOT.war (así la API queda en la raíz)
COPY --from=build /app/**/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]