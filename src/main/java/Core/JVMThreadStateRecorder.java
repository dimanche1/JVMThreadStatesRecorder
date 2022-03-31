package Core;

import Configuration.*;
import Storage.InfluxDBStorage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.influxdb.InfluxDBException;

import java.util.HashMap;
import java.util.Map;

public class JVMThreadStateRecorder {
    private InfluxDBStorage db;
    private InfluxDbConfiguration influxDbConfiguration;
    private ObjectMapper mapper = new ObjectMapper();
    private Map<Integer, GetThreadStates> recorders = new HashMap<>();
    private int counter = 0;

    //TODO Create DB if not exist
    public String influxDbConnect(InfluxDbConfiguration influxDbConfiguration) {
        if(db == null) {
            this.influxDbConfiguration = influxDbConfiguration;

            try {
                db = new InfluxDBStorage(influxDbConfiguration);
                return "InfluxDB connection to " + influxDbConfiguration.getInfluxdbUrl() + " established";
            } catch (InfluxDBException e) {
                return e.toString();
            }
        } else {
            return "Connection to InfluxDB already established.";
        }
    }

    public int start(Configuration configuration) {
        recorders.put(++counter, GetThreadStates.createAndStart(configuration, db, counter));

        return counter;
    }

    public String stop(int jvmThreadStateRecorderID) {
        if (recorders.containsKey(jvmThreadStateRecorderID)) {
            recorders.get(jvmThreadStateRecorderID).stop();
            recorders.remove(jvmThreadStateRecorderID);
            --counter;
            return "Task with id " + jvmThreadStateRecorderID + " stopped.";
        } else {
            return "Task with id " + jvmThreadStateRecorderID + " doesn't exist.";
        }
    }

    public String tasks() throws JsonProcessingException {
        Map<Integer, Configuration> tasks = new HashMap<>();
        recorders.forEach((k, v) -> tasks.put(k, v.getConfiguration()));

        return  mapper.writeValueAsString(tasks);
    }

    public String getDbConfig() throws JsonProcessingException {
        return mapper.writeValueAsString(influxDbConfiguration);
    }

    public boolean internalMonitoring() {
        InternalMonitoring internalMonitoring = new InternalMonitoring(influxDbConfiguration);
        if (InternalMonitoring.getRegistry() != null) {
            return true;
        } else {
            return false;
        }
    }
}
