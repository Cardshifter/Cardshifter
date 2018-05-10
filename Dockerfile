FROM openjdk:8

EXPOSE 4242 4243
VOLUME /data/logs/

ADD . /usr/src/cardshifter
WORKDIR /data/logs
CMD ["java", "-jar", "/usr/src/cardshifter/cardshifter-server/build/libs/cardshifter-server.jar"]
