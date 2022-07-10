FROM openjdk:16-alpine3.13
COPY JVMThreadStatesRecorder.jar /tmp
WORKDIR /tmp
EXPOSE 7080
CMD ["java", "-jar", "/tmp/JVMThreadStatesRecorder.jar","7080", "http://127.0.0.1:8086", "JVMThreadStatesRecorder"]