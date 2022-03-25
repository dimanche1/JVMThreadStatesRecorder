# JVMThreadStatesRecorder

Purpose of the JVMThreadStatesRecorder is to write JVM thread states (like runnable, blocked ...) to the InfluxDB and then visualize those states in the Grafana, etc.

![image](https://user-images.githubusercontent.com/79479018/160123811-21223bf5-466c-4f8d-9652-170e3ac6d905.png)

Command line parameters:
7080 (a port to start web server; a default port: 7070) http://127.0.0.1:8086 (an url to the InfluxDB; optional) jvm_app_monitoring (DB name in the Influxdb; optional)

## Endpoints:

// Configure a connection to the InfluxDB  
POST /InfluxDbConfiguration  
body: {  
"influxdbUrl":"http://127.0.0.1:8086",  
"influxdbDb":"jvm_app_monitoring"  
}  
  
// Start monitoring an application with a pid  
POST /start  
body: {  
"pid":"44620",  
"threadFilter":"Example"  
}  
response: id of the task that can be used for stopping the task  
  
// Start monitoring an application with a jmx host and a jmx port  
POST /start  
body: {  
"jmxHost":"127.0.0.1",  
"jmxPort":"9010",  
"threadFilter":"Example"  
}  
response: id of the task that can be used for stopping the task  
  
// Stop the task with an id  
GET /stop/id={id of the task}  
