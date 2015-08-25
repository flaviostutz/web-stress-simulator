FROM tomcat:8.0.26-jre8

COPY src/web-app/target/*.war /usr/local/tomcat/webapps/

RUN /usr/local/tomcat/bin/startup.sh && sleep 5

EXPOSE 8080
