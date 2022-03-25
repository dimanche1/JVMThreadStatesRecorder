import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.InfluxDBIOException;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class InfluxDBStorage {
    private InfluxDB influxDB;
    private String serverURL;
    private String db;
    private int influxdbBatchSize = 1000;
    private int influxdbBatchTimeSeconds = 10;
    private Map<String, String> additionalTags = new LinkedHashMap<>();

    public InfluxDBStorage(String serverURL, String db) {
        this.serverURL = serverURL;
        this.db = db;

        connect();
    }

    private void connect() {
        System.out.println("Try to connect to influxdb: " + serverURL);

        influxDB = InfluxDBFactory.connect(serverURL);
        influxDB.setDatabase(db);
        influxDB.enableBatch(getInfluxdbBatchSize(), getInfluxdbBatchTimeSeconds(), TimeUnit.SECONDS);
        influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);
        try {
            Pong response = this.influxDB.ping();
            if (response.getVersion().equalsIgnoreCase("unknown")) {
//            log.error("Error pinging server.");
                System.out.println("Error pinging server.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Failed to connect to InfluxDB: " + serverURL);
        }
    }

    public void write(ThreadStateContainer threadStateContainer) {
        if (influxDB == null) connect();

        try {
            influxDB.write(Point.measurement("thread_state")
                    .time(threadStateContainer.getTime(), TimeUnit.MILLISECONDS)
                    .tag(threadStateContainer.getTags())
                    .tag(additionalTags)
                    .fields(threadStateContainer.getFields())
                    .build());
        } catch (InfluxDBIOException ie) {
            System.out.println(ie);
            connect();
        }
    }

    public void close() {
        influxDB.close();
    }

    public Map<String, String> getAdditionalTagsTags() {
        return additionalTags;
    }

    public void setAdditionalTagsTag(String name, String value) {
        additionalTags.put(name, value);
    }

    public int getInfluxdbBatchSize() {
        return influxdbBatchSize;
    }

    public void setInfluxdbBatchSize(int influxdbBatchSize) {
        this.influxdbBatchSize = influxdbBatchSize;
    }

    public int getInfluxdbBatchTimeSeconds() {
        return influxdbBatchTimeSeconds;
    }

    public void setInfluxdbBatchTimeSeconds(int influxdbBatchTimeSeconds) {
        this.influxdbBatchTimeSeconds = influxdbBatchTimeSeconds;
    }
}
