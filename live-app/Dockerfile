#FROM openjdk:8-jdk-alpine
FROM adoptopenjdk/openjdk8-openj9:alpine-slim
RUN apk add tzdata && \
	cp /usr/share/zoneinfo/Europe/Athens /etc/localtime && \
	echo "Europe/Athens" >  /etc/timezone && \
	apk del tzdata && \
    rm -rf /var/cache/apk/
COPY target/opendelos-live-app.jar /opendelos-live-app.jar
ENV JAVA_OPTS=" -XX:+UseSerialGC "
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Xshareclasses -Xquickstart -jar /opendelos-live-app.jar ${0} ${@}"]
#ENTRYPOINT exec java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9994 -jar /opendelos-live-app.jar