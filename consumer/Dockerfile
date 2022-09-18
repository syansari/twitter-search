FROM adoptopenjdk/openjdk11
MAINTAINER io.ansari
COPY build/libs/consumer-0.0.1-SNAPSHOT.jar consumer.jar
ENV AWS_REGION=us-east-1
RUN echo 'Argument copied' $AWS_REGION
ENTRYPOINT ["java", "-jar", "/consumer.jar"]
