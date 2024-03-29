package JVMThreadStatesRecorder.core;

import JVMThreadStatesRecorder.configuration.Configuration;

import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

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
        ThreadInfo[] tinfos = tmbean.getThreadInfo(tids,true, false);
//        ArrayList<ThreadStateContainer> tsc = new ArrayList<ThreadStateContainer>();
         Map<Long, ThreadStateContainer> tsc_m = new LinkedHashMap<>();

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
                tsc_m.put(ti.getThreadId(), threadStateContainer);
//                tsc.add(threadStateContainer);
            }
        }

        long[] threadIds = tmbean.findDeadlockedThreads();

        if (threadIds != null) {
            ThreadInfo[] infos = tmbean.getThreadInfo(threadIds, Integer.MAX_VALUE);
            for (ThreadInfo info : infos) {
                if(tsc_m.get(info.getThreadId()) != null)  {
                    StringBuilder sb = new StringBuilder();
                    StackTraceElement[] stack = info.getStackTrace();
                    sb.append("\tlock owner: " + info.getLockOwnerName() + " @ id: " + info.getLockOwnerId() + "\n");
                    sb.append("\tlock name: " + info.getLockName() + "\n");
                    sb.append("\tstacktrace:\n");

                    for (StackTraceElement elem : stack) {
                        sb.append("\t" + elem + "\n");
                    }

                    tsc_m.get(info.getThreadId()).setField("stack", sb.toString());
                    tsc_m.get(info.getThreadId()).setField("state", "7");
                }
            }
        }
        ArrayList<ThreadStateContainer> tsc = new ArrayList<ThreadStateContainer>(tsc_m.values());
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

