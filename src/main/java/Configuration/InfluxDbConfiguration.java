package Configuration;

public class InfluxDbConfiguration {
    private String influxdbUrl;
    private String influxdbDb = "JVMThreadStatesRecorder";
    private String influxdbMeasurement = "thread_states";
    private int influxdbBatchSize = 1000;
    private int influxdbBatchTime = 10;

    public String getInfluxdbUrl() {
        return influxdbUrl;
    }

    public void setInfluxdbUrl(String influxdbUrl) {
        this.influxdbUrl = influxdbUrl;
    }

    public String getInfluxdbDb() {
        return influxdbDb;
    }

    public void setInfluxdbDb(String influxdbDb) {
        this.influxdbDb = influxdbDb;
    }

    public String getInfluxdbMeasurement() {
        return influxdbMeasurement;
    }

    public void setInfluxdbMeasurement(String influxdbMeasurement) {
        this.influxdbMeasurement = influxdbMeasurement;
    }

    public int getInfluxdbBatchSize() {
        return influxdbBatchSize;
    }

    public void setInfluxdbBatchSize(int influxdbBatchSize) {
        this.influxdbBatchSize = influxdbBatchSize;
    }

    public int getInfluxdbBatchTime() {
        return influxdbBatchTime;
    }

    public void setInfluxdbBatchTime(int influxdbBatchTime) {
        this.influxdbBatchTime = influxdbBatchTime;
    }
}
