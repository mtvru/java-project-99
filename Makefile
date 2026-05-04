setup:
	npm install
	npx build-frontend
	./gradlew clean build

backend:
	./gradlew bootRun --args='--spring.profiles.active=development'

clean:
	./gradlew clean

reload-classes:
	./gradlew -t classes

start-prod:
	./gradlew bootRun --args='--spring.profiles.active=production'

install:
	./gradlew installDist

run-dist:
	java -jar build/libs/app-0.0.1-SNAPSHOT.jar

build:
	./gradlew clean build

test:
	./gradlew clean test

report:
	./gradlew jacocoTestReport

check:
	./gradlew clean check

.PHONY: build
