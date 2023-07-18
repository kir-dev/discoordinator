FROM eclipse-temurin:17-jre-alpine
MAINTAINER kir-dev@sch.bme.hu
COPY build/libs/discoordinator.jar /opt/discoordinator/
WORKDIR /opt/discoordinator
ENTRYPOINT ["java", "-Dspring.profiles.include=docker", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=70", "-jar", "/opt/discoordinator/discoordinator.jar"]
EXPOSE 80
