#FROM openjdk:8-jdk-alpine
FROM adoptopenjdk/openjdk8-openj9:alpine-slim
RUN apk add tzdata && \
	cp /usr/share/zoneinfo/Europe/Athens /etc/localtime && \
	echo "Europe/Athens" >  /etc/timezone && \
	apk del tzdata && \
    rm -rf /var/cache/apk/
COPY target/opendelos-events-app.jar /opendelos-events-app.jar
ENV JAVA_OPTS=" -XX:+UseSerialGC "
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Xshareclasses -Xquickstart -jar /opendelos-events-app.jar ${0} ${@}"]
#Note:msttcorefonts is used for powerpoint font substitution !Important
