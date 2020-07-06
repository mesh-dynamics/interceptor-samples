###################
#build#
#################
FROM cubeiocorp/cubeiobase:0.0.1 AS build
ARG TOKEN
COPY OrderReceiver /
#RUN mkdir ~/.m2
RUN echo "<settings><servers><server><id>github</id><username>x-access-token</username><password>${TOKEN}</password></server></servers></settings>" > ~/.m2/settings.xml
RUN mvn package -DskipTests
##################
# Prod #
#################
FROM openjdk:11.0-jre AS PROD

COPY --from=build /target/OrderReceiver-0.0.1-SNAPSHOT.jar /root.jar
COPY --from=build /samplerconfig.json /tmp/samplerconfig.json

CMD ["java", "-jar", "/root.jar"]
EXPOSE 8080
