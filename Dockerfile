# Build Application Jar
FROM gradle:7.6.0 AS build

WORKDIR /usr/app/

# Copy Hylios project files
COPY . .

# Get username and token used in build.gradle
ARG USERNAME
ARG TOKEN
ENV USERNAME=$USERNAME TOKEN=$TOKEN

RUN gradle shadowJar

# Run Application
FROM openjdk:18.0.1.1-jdk

VOLUME ["/hylios"]
WORKDIR /hylios

# Copy previous builded Jar
COPY --from=build /usr/app/build/libs/Hylios-all.jar /usr/app/Hylios.jar
# Copy entrypoint script
COPY --from=build /usr/app/docker-entrypoint.sh /usr/app/docker-entrypoint.sh

# Add permission to file
RUN chmod +x /usr/app/docker-entrypoint.sh

STOPSIGNAL SIGTERM

# Start application
ENTRYPOINT ["sh", "/usr/app/docker-entrypoint.sh"]