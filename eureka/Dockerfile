FROM openjdk:17-jdk-slim

#도커 외부에서 빌드 한 다음 실행시키는 방법

# JAR 파일 복사 (외부에서 copy 하는 방식 내부에서 빌드를 했기 때문에 가져올 필요 X)
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 실행 명령어
ENTRYPOINT ["java", "-jar", "/app.jar"]