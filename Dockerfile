FROM jetty:9.4-jre8-alpine
ARG BUILD_VERSION

COPY audio-splitter-${BUILD_VERSION}.war /var/lib/jetty/webapps/audio-splitter-${BUILD_VERSION}.war
CMD ["java","-jar","/usr/local/jetty/start.jar"]