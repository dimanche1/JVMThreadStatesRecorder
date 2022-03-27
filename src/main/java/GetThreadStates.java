import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetThreadStates implements Runnable{
    private MBeanConnection mBeanConnection = new MBeanConnection();
    private MBeanServerConnection server;
    private Configuration configuration;
    private boolean isRunning = false;
    private InfluxDBStorage db;
    private  ThreadState ts;
    private ExecutorService exec;

    private final static long GET_STATES_EACH_MS = 1000;

    GetThreadStates(Configuration configuration, InfluxDBStorage db) {
        this.configuration = configuration;
        this.db = db;

        if (configuration.getPid() != null) {
            connectByPid();
        } else if (configuration.getJmxHost() != null && String.valueOf(configuration.getJmxPort()) != null) {
            connectByJmxRemote();
        }
    }

    public static GetThreadStates createAndStart(Configuration configuration, InfluxDBStorage db) {
        GetThreadStates getThreadStates = new GetThreadStates(configuration, db);

        getThreadStates.ts = new ThreadState(getThreadStates.server);

        getThreadStates.exec = Executors.newSingleThreadExecutor();

        getThreadStates.exec.execute(getThreadStates);

        return getThreadStates;
    }

    @Override
    public void run() {
        setRunning(true);

        while (isRunning()) {
            long beforeTime = System.currentTimeMillis();

            ArrayList<ThreadStateContainer> threadList = ts.getThreadStates(getConfiguration().getThreadFilter());
            for (ThreadStateContainer threadListElement : threadList) {
                if (getConfiguration().getPid() != null) {
                    threadListElement.setTag("pid", getConfiguration().getPid());
                } else {
                    threadListElement.setTag("jmx_host", getConfiguration().getJmxHost());
                    threadListElement.setTag("jmx_port", String.valueOf(getConfiguration().getJmxPort()));
                }

                getConfiguration().getTags().forEach((k, v) -> threadListElement.setTag(k, v));

                db.write(threadListElement);
            }
            try {
                long afterTime = System.currentTimeMillis();
                long gapTime = afterTime - beforeTime;
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

        getExec().shutdown();
    }

    public void connectByPid() {
        server = mBeanConnection.getServerConnectionByPID(getConfiguration().getPid());
    }

    public void connectByJmxRemote() {
        server = mBeanConnection.getServerConnectionRemote(getConfiguration().getJmxHost(), getConfiguration().getJmxPort());
    }

    public ExecutorService getExec() {
        return exec;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
