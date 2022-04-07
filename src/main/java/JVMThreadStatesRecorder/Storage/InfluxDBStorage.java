package JVMThreadStatesRecorder.Storage;

import JVMThreadStatesRecorder.Configuration.InfluxDbConfiguration;
import JVMThreadStatesRecorder.Core.ThreadStateContainer;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBException;
import org.influxdb.InfluxDBFactory;
import org.influxdb.InfluxDBIOException;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Query;

import java.util.concurrent.TimeUnit;

public class InfluxDBStorage {
    private InfluxDB influxDB;
    private InfluxDbConfiguration influxDbConfiguration;
    private boolean isConnected = false;

    public InfluxDBStorage(InfluxDbConfiguration influxDbConfiguration) {
        this.influxDbConfiguration = influxDbConfiguration;

        if (influxDbConfiguration.getInfluxdbUrl() != null && influxDbConfiguration.getInfluxdbDb() != null ) {
            connect();
        }
    }

    private void connect() {
        System.out.println("Try to connect to influxdb: " + influxDbConfiguration.getInfluxdbUrl());

        influxDB = InfluxDBFactory.connect(influxDbConfiguration.getInfluxdbUrl());
        influxDB.query(new Query("CREATE DATABASE " + influxDbConfiguration.getInfluxdbDb()));
        influxDB.setDatabase(influxDbConfiguration.getInfluxdbDb());
        influxDB.enableBatch(influxDbConfiguration.getInfluxdbBatchSize(), influxDbConfiguration.getInfluxdbBatchTime(), TimeUnit.SECONDS);
        influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);

        try {
            Pong response = this.influxDB.ping();
            if (response.getVersion().equalsIgnoreCase("unknown")) {
                System.out.println("Error pinging InfluxDB server " + influxDbConfiguration.getInfluxdbUrl());
                return;
            } else {
                isConnected = true;
            }
        } catch (Exception e) {
            throw new InfluxDBException("Failed to connect to InfluxDB " + influxDbConfiguration.getInfluxdbUrl());
        }
    }

    public void write(ThreadStateContainer threadStateContainer) {
        if (influxDB == null) connect();

        try {
            influxDB.write(Point.measurement(influxDbConfiguration.getInfluxdbMeasurement())
                    .time(threadStateContainer.getTime(), TimeUnit.MILLISECONDS)
                    .tag(threadStateContainer.getTags())
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

    public boolean isConnected() {
        return isConnected;
    }
}
