package JVMThreadStatesRecorder.Core;

import JVMThreadStatesRecorder.Configuration.Configuration;
import JVMThreadStatesRecorder.Storage.InfluxDBStorage;
import io.micrometer.core.instrument.Timer;

import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class GetThreadStates implements Runnable {
    private int id;
    private MBeanConnection mBeanConnection = new MBeanConnection();
    private MBeanServerConnection server;
    private Configuration configuration;
    private boolean isRunning = false;
    private InfluxDBStorage db;
    private  ThreadState ts;
    private ExecutorService exec;
    private Timer timer;

    private final static long GET_STATES_EACH_MS = 1000;

    GetThreadStates(Configuration configuration, InfluxDBStorage db, int id) {
        this.configuration = configuration;
        this.db = db;
        this.id = id;

        if (configuration.getPid() != null) {
            connectByPid();
        } else if (configuration.getJmxHost() != null && String.valueOf(configuration.getJmxPort()) != null) {
            connectByJmxRemote();
        }

        setTimer();
    }

    public static GetThreadStates createAndStart(Configuration configuration, InfluxDBStorage db, int id) {
        GetThreadStates getThreadStates = new GetThreadStates(configuration, db, id);

        getThreadStates.ts = new ThreadState(getThreadStates.server, configuration);

        getThreadStates.exec = Executors.newSingleThreadExecutor(new NamedThreadFactory(id));

        getThreadStates.exec.execute(getThreadStates);

        return getThreadStates;
    }

    @Override
    public void run() {
        setRunning(true);

        while (isRunning()) {
            long beforeTime = System.currentTimeMillis();

            ArrayList<ThreadStateContainer> threadList = ts.getThreadStates(configuration);
            for (ThreadStateContainer threadListElement : threadList) {
                if (getConfiguration().getPid() != null) {
//                    threadListElement.setTag("pid", getConfiguration().getPid());
                    threadListElement.setTag("task", id + " - " + getConfiguration().getPid());
                } else {
//                    threadListElement.setTag("jmx_host", getConfiguration().getJmxHost());
//                    threadListElement.setTag("jmx_port", String.valueOf(getConfiguration().getJmxPort()));
                    threadListElement.setTag("task", id + " - " + getConfiguration().getJmxHost() + ":" + getConfiguration().getJmxPort());
                }

                getConfiguration().getTags().forEach((k, v) -> threadListElement.setTag(k, v));

                db.write(threadListElement);
            }
            try {
                long afterTime = System.currentTimeMillis();
                long gapTime = afterTime - beforeTime;
                if (timer != null) {
                    timer.record(gapTime, TimeUnit.MILLISECONDS);
                }
                if (gapTime < GET_STATES_EACH_MS) {
                    Thread.sleep(GET_STATES_EACH_MS - gapTime);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public void stop() {
        isRunning = false;

        try {
            mBeanConnection.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(timer != null) {
            InternalMonitoring.getInfluxMeterRegistry().remove(timer);
        }

        getExec().shutdown();
    }

    private void connectByPid() {
        server = mBeanConnection.getServerConnectionByPID(getConfiguration().getPid());
    }

    private void connectByJmxRemote() {
        server = mBeanConnection.getServerConnectionRemote(getConfiguration().getJmxHost(), getConfiguration().getJmxPort());
    }

    public void setTimer() {
        if (InternalMonitoring.getInfluxMeterRegistry() != null) {
            String task = "";
            if (getConfiguration().getPid() != null) {
                task = id + " - " + getConfiguration().getPid();
            } else {
                task = id + " - " + getConfiguration().getJmxHost() + ":" + getConfiguration().getJmxPort();
            }
            timer = Timer
                    .builder("time_to_get_thread_states")
                    .description("Time to get thread states by tasks")
                    .tags("task", task)
                    .register(InternalMonitoring.getInfluxMeterRegistry());
        }
    }

    public Timer getTimer() {
        return timer;
    }

    public ExecutorService getExec() {
        return exec;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public int getId() {
        return id;
    }

    static class NamedThreadFactory implements ThreadFactory {
        private int id;

        NamedThreadFactory(int id) {
            this.id = id;
        }

        public Thread newThread(Runnable r) {
            return new Thread(r, "JVMThreadStatesRecorder-" + id);
        }
    }

//    private Point getInfluxDbPoint(ThreadStateContainer threadStateContainer) {
//        return Point.measurement(influxDbConfiguration.getInfluxdbMeasurement())
//                .time(threadStateContainer.getTime(), TimeUnit.MILLISECONDS)
//                .tag(threadStateContainer.getTags())
//                .fields(threadStateContainer.getFields())
//                .build();
//    }
}
