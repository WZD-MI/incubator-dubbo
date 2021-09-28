FROM maven:3-jdk-11-slim

WORKDIR /opt

RUN apt-get update \
    && apt-get install -y gcc zlib1g-dev libstdc++-10-dev \
    && curl -L -o /opt/graalvm-ce.tar.gz "https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-21.1.0/graalvm-ce-java11-linux-amd64-21.1.0.tar.gz" \
    && tar -xf graalvm-ce.tar.gz \
    && /opt/graalvm-ce-java11-21.1.0/bin/gu install native-image \
    && rm graalvm-ce.tar.gz

ENV PATH=/opt/graalvm-ce-java11-21.1.0/bin/:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
ENV JAVA_HOME=/opt/graalvm-ce-java11-21.1.0
