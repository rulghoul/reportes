ARG BUILD_IMAGE=maven:3.9.12-eclipse-temurin-21
ARG RUNTIME_IMAGE=eclipse-temurin:21-jre-alpine
#############################################################################################
###                Stage where Docker is pulling all maven dependencies                   ###
#############################################################################################
FROM ${BUILD_IMAGE} AS dependencies
WORKDIR /app
COPY pom.xml ./
COPY ./*.config ./
RUN mvn -B dependency:go-offline
#############################################################################################
###              Stage where Docker is building spring boot app using maven               ###
#############################################################################################
FROM dependencies AS build
COPY src ./src
RUN mkdir ./target
RUN mvn -B clean package

#############################################################################################
###   Image config                                   Main stage                           ###
#############################################################################################
FROM ${RUNTIME_IMAGE} AS main
ARG TARGET_DIR=target
WORKDIR /opt/app
EXPOSE 8080
COPY --from=build /app/target/*.jar /opt/app/service.jar
ENTRYPOINT ["java", \
  "-Duser.language=es", \
  "-Duser.country=MX", \
  "-Duser.region=MX", \
  "-Dfile.encoding=UTF-8", \
  "-jar","/opt/app/service.jar"]