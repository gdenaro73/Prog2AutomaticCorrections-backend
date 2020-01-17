#base image containing Java runtime
FROM openjdk:8-jdk-alpine

# Add a volume pointing to /tmp
VOLUME /tmp

# The application's jar file
ARG JAR_FILE=target/Prog2AutomaticCorrections-1.1.0-SNAPSHOT.jar

# Add the application's jar to the container
ADD ${JAR_FILE} p2ac.jar

# Prepare environment.
# Create needed folders
RUN mkdir /home/p2ac && \
	mkdir /home/p2ac/libs && \
    mkdir /home/p2ac/libs/compilelib  && \
    mkdir /home/p2ac/libs/runlib
    
# Add needed libs for compile and run
ADD ./src/main/resources/compilelib /home/p2ac/libs/compilelib
ADD ./src/main/resources/runlib /home/p2ac/libs/runlib
    
RUN sh -c 'touch /p2ac.jar'

# Run the jar file 
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=server","-jar","/p2ac.jar"]
