FROM openjdk:8

EXPOSE 4242 4243
VOLUME /data/logs/

ADD . /usr/src/cardshifter
WORKDIR /usr/src/cardshifter/build/libs/
CMD ["java", "-jar", "cardshifter-server.jar"]
