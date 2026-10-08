# 1. Compilar con Maven
FROM maven:3.8.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests -f GastoFacilWeb/pom.xml

# 2. Servidor Tomcat 10 (Soporte Jakarta EE 10)
FROM tomcat:10.1-jdk17-corretto

# Vaciar apps por defecto
RUN rm -rf /usr/local/tomcat/webapps/*

# Desactivar puerto de apagado de Tomcat para evitar bloqueos con los health-checks de Render
RUN sed -i 's/port="8005" shutdown="SHUTDOWN"/port="-1" shutdown="SHUTDOWN"/' /usr/local/tomcat/conf/server.xml

# Habilitar filtro CORS nativo global en web.xml de Tomcat 10
RUN sed -i '/<\/web-app>/i \
  <filter>\n\
    <filter-name>CorsFilter</filter-name>\n\
    <filter-class>org.apache.catalina.filters.CorsFilter</filter-class>\n\
    <init-param>\n\
      <param-name>cors.allowed.origins</param-name>\n\
      <param-value>*</param-value>\n\
    </init-param>\n\
    <init-param>\n\
      <param-name>cors.allowed.methods</param-name>\n\
      <param-value>GET,POST,HEAD,OPTIONS,PUT,DELETE</param-value>\n\
    </init-param>\n\
    <init-param>\n\
      <param-name>cors.allowed.headers</param-name>\n\
      <param-value>Content-Type,X-Requested-With,accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization,X-Usuario-Id,idTienda</param-value>\n\
    </init-param>\n\
  </filter>\n\
  <filter-mapping>\n\
    <filter-name>CorsFilter</filter-name>\n\
    <url-pattern>/*</url-pattern>\n\
  </filter-mapping>' /usr/local/tomcat/conf/web.xml

# Copiar el .war compilado como ROOT.war
COPY --from=build /app/GastoFacilWeb/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]