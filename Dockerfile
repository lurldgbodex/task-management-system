FROM eclipse-temurin:17-jre-alpine as build
LABEL authors="lurldgbodex"
WORKDIR /workspace/app

RUN mkdir -p target/extracted

ADD target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract --destination target/extracted

FROM eclipse-temurin:17-jre-alpine
ARG WORKDIR=/workspace/app
WORKDIR ${WORKDIR}
VOLUME /tmp
ARG EXTRACTED=${WORKDIR}/target/extracted
COPY --from=build ${EXTRACTED}/dependencies/ ./
COPY --from=build ${EXTRACTED}/spring-boot-loader/ ./
COPY --from=build ${EXTRACTED}/snapshot-dependencies/ ./
COPY --from=build ${EXTRACTED}/application/ ./

ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-docker}", "org.springframework.boot.loader.launch.JarLauncher"]