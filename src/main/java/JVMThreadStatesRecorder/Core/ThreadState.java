package JVMThreadStatesRecorder.Core;

import JVMThreadStatesRecorder.Configuration.Configuration;

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

    public ThreadState(MBeanServerConnection server, Configuration configuration) {
        this.server = server;
        if (tmbean == null) {
            try {
                tmbean = newPlatformMXBeanProxy(server, THREAD_MXBEAN_NAME, ThreadMXBean.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

       if (configuration.isContentionMonitoring()) {
           // Enables or disables thread contention monitoring. Thread contention monitoring is disabled by default.
           tmbean.setThreadContentionMonitoringEnabled(true);
       }
    }

    public ArrayList getThreadStates(Configuration configuration) {
//        if (tmbean == null) {
//            try {
//                tmbean = newPlatformMXBeanProxy(server, THREAD_MXBEAN_NAME, ThreadMXBean.class);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        long[] tids = tmbean.getAllThreadIds();
        ThreadInfo[] tinfos = tmbean.getThreadInfo(tids);
        ArrayList<ThreadStateContainer> tsc = new ArrayList<ThreadStateContainer>();

        for (ThreadInfo ti : tinfos) {
            if (ti == null) continue;

            if (ti.getThreadName().contains(configuration.getThreadFilter())) {
                ThreadStateContainer threadStateContainer = new ThreadStateContainer();

                threadStateContainer.setTime(System.currentTimeMillis());
                if(configuration.isThreadStates() && configuration.isContentionMonitoring()) {
                    threadStateContainer.setTag("id", String.valueOf(ti.getThreadId()));
                    threadStateContainer.setTag("name", ti.getThreadName());
                    threadStateContainer.setField("state", getThreadStateInt(ti.getThreadState().toString()));
                    threadStateContainer.setField("blockedCount", ti.getBlockedCount());
                    threadStateContainer.setField("blockedTime", ti.getBlockedTime());
                }
                else if (configuration.isThreadStates() && !configuration.isContentionMonitoring()) {
                    threadStateContainer.setTag("id", String.valueOf(ti.getThreadId()));
                    threadStateContainer.setTag("name", ti.getThreadName());
                    threadStateContainer.setField("state", getThreadStateInt(ti.getThreadState().toString()));
                }
                else {
                    threadStateContainer.setTag("id", String.valueOf(ti.getThreadId()));
                    threadStateContainer.setTag("name", ti.getThreadName());
                    threadStateContainer.setField("blockedCount", ti.getBlockedCount());
                    threadStateContainer.setField("blockedTime", ti.getBlockedTime());
                }
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

