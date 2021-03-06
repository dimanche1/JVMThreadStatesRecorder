# JVMThreadStatesRecorder

Purpose of the JVMThreadStatesRecorder is to write JVM thread states (like runnable, blocked ...) to the InfluxDB and then visualize those states in the Grafana, etc. 🧵

![image](https://user-images.githubusercontent.com/79479018/161306520-70a44e5b-c9ce-4c46-9a9a-0f64bc73617a.png)


## Command line (InfluxDB parameters are optional, could be configured with /influxdb-configuration):
java -jar JVMThreadStatesRecorder.jar 7080 http://127.0.0.1:8086 JVMThreadStatesRecorder
1. java (version 11 or later)  
2. JVMThreadStatesRecorder.jar can be taken from out/artifacts/JVMThreadStatesRecorder_jar/  
3. 7080 (a port to start web server; a default port: 7070)
4. http://127.0.0.1:8086 (an url to the InfluxDB; optional)
5. JVMThreadStatesRecorder (DB name in the Influxdb; optional)

## Endpoints

- #### Configure a connection to the InfluxDB
POST /influxdb-configuration  
body:
```json
{  
"influxdbUrl":"http://127.0.0.1:8086",  
"influxdbDb":"JVMThreadStatesRecorder",  
"influxdbMeasurement":"thread_states",  
"influxdbBatchSize":"1000",  
"influxdbBatchTime":"10"  
}
```
"influxdbDb":"jvm_app_monitoring",  *(optional, default: JVMThreadStatesRecorder)*  
"influxdbMeasurement":"thread_states",  *(optional, default: thread_states)*  
"influxdbBatchSize":"1000",  *(optional, default: 1000)*  
"influxdbBatchTime":"10"  *(optional, default: 10)*

- #### Start monitoring an application with a jmx host and a jmx port
POST /start  
body:
```json
{  
"jmxHost":"127.0.0.1",  
"jmxPort":"9010",  
"threadFilter":"Example",
"threadStates":"true",   
"contentionMonitoring":"true",  
"tags":{  
"tag1":"value1",  
"tag2":"value2"  
}  
}  
```
response: id of the task that can be used for stopping the task  
"threadFilter":"Example" *(optional, default: all threads, carefull, there could be hundreds of threads)*
"threadStates":"true" *(true or false. Enable thread states monitoring)*  
"contentionMonitoring":"true" *(true or false. Enable contention monitoring, blocked count and blocked time)*  
"tags":{....} *(optional, additional tags for enriching data in the InfluxDB)*

- #### Start monitoring an application with a pid
POST /start  
body:
```json
{  
"pid":"44620",  
"threadFilter":"Example",
"threadStates":"true",
"contentionMonitoring":"true", 
"tags":{  
"tag1":"value1",  
"tag2":"value2"  
}  
}  
```
response: id of the task that can be used for stopping the task  
"threadFilter":"Example" *(optional, default: all threads, carefull, there could be hundreds of threads)*  
"threadStates":"true" *(true or false. Enable thread states monitoring)*  
"contentionMonitoring":"true" *(true or false. Enable contention monitoring, blocked count and blocked time)*  
"tags":{....} *(optional, additional tags for enriching data in the InfluxDB)*

- #### Stop the task with an id
GET /stop/id={id of the task}

- #### List of currently running tasks
GET /tasks  
response:
```
{  
  (id of the task):  
  {  
    (Configuration.Configuration object of the task)  
  },  
  ...
}    
```  

- #### Returns connection configuration to the InfluxDB
GET /dbconfig

- #### Start internal monitoring
GET /internal-monitoring  
Shows time in ms. which is spent on collecting thread states, grouped by a task

## Grafana configuration
- #### Ready to use dashboard
:chart_with_upwards_trend: [JVM_Thread_States_Dashboard.json](JVM_Thread_States_Dashboard.json)   
Tested with Grafana 8.4.3

- #### Visaulization: State timeline

- #### Possible query
![image](https://user-images.githubusercontent.com/79479018/160241944-03c1b717-69c8-42be-afd5-b1e65a025a09.png)

- #### Value mappings
![image](https://user-images.githubusercontent.com/79479018/160242159-ab79b34a-6213-4728-ab64-8193456e6795.png)


## Contention monitoring  
Shows total number of times that a thread blocked to enter or reenter a monitor and time that a thread has blocked to enter or reenter a monitor  
:chart_with_upwards_trend: [JVM_Thread_States_Dashboard.json](JVM_Thread_States_Dashboard.json)  

![image](https://user-images.githubusercontent.com/79479018/163650558-d488a2da-b4ea-44e7-b107-bdb8ac77e477.png)    

![image](https://user-images.githubusercontent.com/79479018/163650616-1844ce9c-b62e-4bc9-826c-2a6590c84639.png)    


## Internal monitoring
Shows time in ms. which is spent on collecting thread states, grouped by a task   
:chart_with_upwards_trend: [JVM_Thread_States_Dashboard.json](JVM_Thread_States_Dashboard.json)

![image](https://user-images.githubusercontent.com/79479018/161435702-f2e92699-dbb6-47a1-9018-81fa01719538.png)  
