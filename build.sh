# 使用 JAVA_HOME 环境变量指定 JDK
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
mvn clean package -DskipTests
docker build -t selfstudy-room:1.0 .
