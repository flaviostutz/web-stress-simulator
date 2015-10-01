FROM flaviostutz/tomcat-base:8-maven3-jdk8

COPY src/web-app /opt/src/web-app

RUN cd /opt/src/web-app && mvn clean install && mv target/*.war /usr/local/tomcat/webapps/


