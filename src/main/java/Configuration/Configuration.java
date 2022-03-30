package Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

public class Configuration {
    private String pid;
    private String jmxHost;
    private int jmxPort;
    private String threadFilter = "";
    private Map<String, String> tags = new LinkedHashMap<>();

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getJmxHost() {
        return jmxHost;
    }

    public void setJmxHost(String jmxHost) {
        this.jmxHost = jmxHost;
    }

    public int getJmxPort() {
        return jmxPort;
    }

    public void setJmxPort(int jmcPort) {
        this.jmxPort = jmcPort;
    }

    public String getThreadFilter() {
        return threadFilter;
    }

    public void setThreadFilter(String threadFilter) {
        this.threadFilter = threadFilter;
    }

    public Map<String, String> getTags() {
        return tags;
    }

//    public void setTags(String name, String value) {
//        tags.put(name, value);
//    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}
