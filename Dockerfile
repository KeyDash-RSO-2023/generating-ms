# Start with a base image containing Java runtime
#FROM openjdk:21-ea-1-oracle as build

# Add Maintainer Info
#LABEL maintainer="ak84795@student.uni-lj.si"

# Add a volume pointing to /tmp
#VOLUME /tmp

# Make port 8080 available to the world outside this container
#EXPOSE 8080

# The application's jar file
#ARG JAR_FILE=target/*.jar

# Add the application's jar to the container
#ADD ${JAR_FILE} app.jar

# Run the jar file
#ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
FROM maven:3.8.3-openjdk-17 AS build
COPY ./ /app
WORKDIR /app
RUN mvn --show-version --update-snapshots --batch-mode clean package

FROM eclipse-temurin:17-jre
RUN mkdir /app
WORKDIR /app
COPY --from=build ./app/api/target/users-api-1.0.0-SNAPSHOT.jar /app
EXPOSE 8080
CMD ["java", "-jar", "generator-service-1.0.0-SNAPSHOT.jar"]
