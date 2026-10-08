FROM maven:3.8.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests -f GastoFacilWeb/pom.xml

# Usar servidor Tomcat 9 para desplegar la aplicación Java
FROM tomcat:9.0-jdk17-corretto
RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY --from=build /app/GastoFacilWeb/target/*.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]