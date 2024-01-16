FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

COPY . /workspace/app
RUN echo $(ls -1 /workspace/app)
RUN target=/root/.gradle ./gradlew clean build
RUN echo $(ls -1 /workspace/app)
RUN mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*-1.0.0.jar)

FROM eclipse-temurin:17-jre-alpine
WORKDIR /workspace/app
COPY --from=build /workspace/app/build/libs/*1.0.0.jar /workspace/app/app.jar
EXPOSE 8083
#RUN echo $(ls -1 /workspace/app)
ENTRYPOINT ["java", "-jar", "/workspace/app/app.jar"]