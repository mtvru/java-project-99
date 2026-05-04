FROM gradle:9.2-jdk21

RUN apt-get update && apt-get install -y nodejs npm make

WORKDIR /app

RUN curl -L https://repo1.maven.org/maven2/io/sentry/sentry-opentelemetry-agent/8.40.0/sentry-opentelemetry-agent-8.40.0.jar -o /app/sentry-agent.jar
RUN mkdir -p src/main/resources/certs
RUN openssl genpkey -out src/main/resources/certs/private.pem -algorithm RSA -pkeyopt rsa_keygen_bits:2048 && \
    openssl rsa -in src/main/resources/certs/private.pem -pubout -out src/main/resources/certs/public.pem

COPY ./gradle /app/gradle
COPY ./src /app/src
COPY ./build.gradle.kts /app/build.gradle.kts
COPY ./gradlew /app/gradlew
COPY ./settings.gradle.kts /app/settings.gradle.kts
COPY ./Makefile /app/Makefile
COPY ./package.json /app/package.json
COPY ./package-lock.json /app/package-lock.json

RUN make setup

ENV JAVA_OPTS="-Xmx512M -Xms512M"
ENV SENTRY_AUTO_INIT=false
ENV JAVA_TOOL_OPTIONS="-javaagent:/app/sentry-agent.jar"
EXPOSE 8080
CMD ["java", "-jar", "build/libs/app-0.0.1-SNAPSHOT.jar"]
