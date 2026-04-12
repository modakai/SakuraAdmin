# 贝尔实验室 Spring 官方推荐镜像 JDK下载地址 https://bell-sw.com/pages/downloads/
FROM bellsoft/liberica-openjdk-debian:17.0.11-cds

LABEL maintainer="sakura"

# todo 项目自己的项目名称
RUN mkdir -p /sakura/server/logs \
    /sakura/server/temp \
    /sakura/skywalking/agent

WORKDIR /sakura/server

ENV SERVER_PORT=8101 LANG=C.UTF-8 LC_ALL=C.UTF-8 JAVA_OPTS=""

EXPOSE ${SERVER_PORT}

ADD ./target/springboot-init-0.0.1-SNAPSHOT.jar ./app.jar

ENTRYPOINT java -Dserver.port=${SERVER_PORT} \
           # 应用名称 如果想区分集群节点监控 改成不同的名称即可
           #-Dskywalking.agent.service_name=yuba-server \
           #-javaagent:/yuba/skywalking/agent/skywalking-agent.jar \
           -XX:+HeapDumpOnOutOfMemoryError -XX:+UseZGC ${JAVA_OPTS} \
           -jar app.jar


