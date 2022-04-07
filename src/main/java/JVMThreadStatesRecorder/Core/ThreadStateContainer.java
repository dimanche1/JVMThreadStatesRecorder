package JVMThreadStatesRecorder.Core;

import java.util.LinkedHashMap;
import java.util.Map;

public class ThreadStateContainer {
    private long time;
    private Map<String, String> tags = new LinkedHashMap<>();
    private Map<String, Object> fields = new LinkedHashMap<>();

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTag(String name, String value) {
        tags.put(name, value);
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setField(String name, Object value) {
        fields.put(name, value);
    }
}
