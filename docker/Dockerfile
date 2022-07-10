FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine-slim
COPY JVMThreadStatesRecorder.jar /tmp
WORKDIR /tmp
CMD ["java", "-jar", "/tmp/JVMThreadStatesRecorder.jar","7080", "http://127.0.0.1:8086", "JVMThreadStatesRecorder"]