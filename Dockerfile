FROM maven:3.8-openjdk-17

# 设置时区
ENV TZ=Asia/Shanghai

EXPOSE 10086

ENV SERVER_DIR=/owl-red-packet

WORKDIR $SERVER_DIR

COPY . .

# 打包并设置包名
RUN mvn clean package -DskipTests -Dspring.profiles.active=dev

# 设置包名
RUN mv ./target/owl-red-packet-server-1.0.jar ./owl-red-packet-server.jar

# 通过 Java 命令启动应用
ENTRYPOINT ["nohup", "java", "-Duser.timezone=Asia/Shanghai", "-jar", "$SERVER_DIR/owl-red-packet-server.jar", ">log-prod", "&"]
