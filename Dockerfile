FROM gradle:9.2-jdk21

WORKDIR /app

RUN mkdir -p src/main/resources/certs
RUN openssl genpkey -out src/main/resources/certs/private.pem -algorithm RSA -pkeyopt rsa_keygen_bits:2048 && \
    openssl rsa -in src/main/resources/certs/private.pem -pubout -out src/main/resources/certs/public.pem

COPY ./gradle /app/gradle
COPY ./src /app/src
COPY ./build.gradle.kts /app/build.gradle.kts
COPY ./gradlew /app/gradlew
COPY ./settings.gradle.kts /app/settings.gradle.kts

RUN ./gradlew clean bootJar
ENV JAVA_OPTS="-Xmx512M -Xms512M"
EXPOSE 8080
CMD ["java", "-jar", "build/libs/hexlet-spring-blog-0.0.1-SNAPSHOT.jar"]
