FROM openjdk:8

EXPOSE 4242 4243
VOLUME /data/logs/

ADD . /usr/src/cardshifter
WORKDIR /usr/src/cardshifter
CMD ["java", "-jar", "/usr/src/cardshifter/build/libs/cardshifter-server.jar"]
