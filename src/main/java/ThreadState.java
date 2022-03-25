import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;

import static java.lang.management.ManagementFactory.THREAD_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.newPlatformMXBeanProxy;

public class ThreadState {
    private MBeanServerConnection server;
    private ThreadMXBean tmbean;

    public ThreadState(MBeanServerConnection server) {
        this.server = server;
    }

    public ArrayList getThreadStates(String threadFilter) {
        if (tmbean == null) {
            try {
                tmbean = newPlatformMXBeanProxy(server, THREAD_MXBEAN_NAME, ThreadMXBean.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        long[] tids = tmbean.getAllThreadIds();
        ThreadInfo[] tinfos = tmbean.getThreadInfo(tids);
        ArrayList<ThreadStateContainer> tsc = new ArrayList<ThreadStateContainer>();

        for (ThreadInfo ti : tinfos) {
            if (ti == null) continue;

            if (ti.getThreadName().contains(threadFilter)) {
                ThreadStateContainer threadStateContainer = new ThreadStateContainer();

                threadStateContainer.setTime(System.currentTimeMillis());
                threadStateContainer.setTag("id", String.valueOf(ti.getThreadId()));
                threadStateContainer.setTag("name", ti.getThreadName());
                threadStateContainer.setField("state", ti.getThreadState().toString());
                threadStateContainer.setField("state", getThreadStateInt(ti.getThreadState().toString()));

                tsc.add(threadStateContainer);
            }
        }
        return tsc;
    }

    private String getThreadStateInt(String threadState) {
        switch (threadState) {
            case ("NEW"): return "1";
            case ("RUNNABLE"): return "2";
            case ("BLOCKED"): return "3";
            case ("WAITING"): return "4";
            case ("TIMED_WAITING"): return "5";
            case ("TERMINATED"): return "6";
            default: return "0";
        }
    }
}

