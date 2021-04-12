FROM openjdk:11.0.10-jdk-slim
COPY target/scala-2.13/bookworm.jar bookworm.jar
EXPOSE 9990
CMD ["java", "-jar", "bookworm.jar"]

