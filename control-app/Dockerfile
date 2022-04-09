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
COPY target/opendelos-control-app.jar /opendelos-control-app.jar
ENV JAVA_OPTS=" -XX:+UseSerialGC "
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Xshareclasses -Xquickstart -jar /opendelos-control-app.jar ${0} ${@}"]
#ENTRYPOINT exec java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9991 -jar /opendelos-dashboard-service.jar
#Note:msttcorefonts is used for powerpoint font substitution !Important