FROM eclipse-temurin:17-jdk-alpine

RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone

ADD ./target/SelfStudyRoom-1.0.jar /app/
CMD ["java", "-Xmx200m",  "-jar", "/app/SelfStudyRoom-1.0.jar"]

EXPOSE 8079