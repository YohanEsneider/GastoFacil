# Usar imagen con Java y Maven para compilar
FROM maven:3.8.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .

# Buscar y compilar el pom.xml automáticamente en cualquier subcarpeta
RUN find . -name "pom.xml" -exec mvn clean package -DskipTests -f {} \;

# Usar servidor Tomcat 9 para desplegar la aplicación Java
FROM tomcat:9.0-jdk17-corretto
RUN rm -rf /usr/local/tomcat/webapps/ROOT
# Copiar el archivo .war generado sin importar en qué subcarpeta target quedó
COPY --from=build /app/**/target/*.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]