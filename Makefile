test:
	./gradlew clean test

bootRun:
	SPRING_PROFILES_ACTIVE=development ./gradlew bootRun

report:
	./gradlew jacocoTestReport

check:
	./gradlew clean check

.PHONY: build
