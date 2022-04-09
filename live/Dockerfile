#FROM openjdk:8-jdk-alpine
FROM adoptopenjdk/openjdk8-openj9:alpine-slim
RUN apk add curl bash ffmpeg tzdata && \
    apk --no-cache add msttcorefonts-installer fontconfig && \
    update-ms-fonts && \
    fc-cache -f && \
	cp /usr/share/zoneinfo/Europe/Athens /etc/localtime && \
	echo "Europe/Athens" >  /etc/timezone && \
	apk del tzdata && \
    rm -rf /var/cache/apk/
COPY target/opendelos-liverunner-service.jar /opendelos-liverunner-service.jar
ENV JAVA_OPTS=" -XX:+UseSerialGC "
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Xshareclasses -Xquickstart -jar /opendelos-liverunner-service.jar ${0} ${@}"]
#ENTRYPOINT exec java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9993 -jar /opendelos-liverunner-service.jar