FROM adoptopenjdk/openjdk8-openj9:alpine-slim
#FROM openjdk:8-jdk-alpine
COPY target/opendelos-root-app.jar /opendelos-root-app.jar
ENV JAVA_OPTS=" -XX:+UseSerialGC "
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Xshareclasses -Xquickstart -jar /opendelos-root-app.jar ${0} ${@}"]
#ENTRYPOINT exec java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9995 -jar /opendelos-root-app-service.jar