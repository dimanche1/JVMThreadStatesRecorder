import java.util.HashMap;
import java.util.Map;

public class JVMThreadStateRecorder {
    private InfluxDBStorage db;
    private Map<Integer, GetThreadStates> recorders = new HashMap<>();
    private int counter = 0;

    public String dbConfiguration(InfluxDbConfiguration influxDbConfiguration) {
        if(db == null) {
            db = new InfluxDBStorage(influxDbConfiguration);

            return "InfluxDB connection to " + influxDbConfiguration.getInfluxdbUrl() + " established";
        } else {
            return "Connection to InfluxDB already established.";
        }
    }

    public int start(Configuration configuration) {
        recorders.put(++counter, GetThreadStates.createAndStart(configuration, db));

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
}
