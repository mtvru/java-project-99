run-dist:
	java -jar build/libs/app-0.0.1-SNAPSHOT.jar

build:
	./gradlew clean bootJar

test:
	./gradlew clean test

bootRun:
	SPRING_PROFILES_ACTIVE=development ./gradlew bootRun

report:
	./gradlew jacocoTestReport

check:
	./gradlew clean check

.PHONY: build
