package Core;

import Configuration.InfluxDbConfiguration;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.influx.InfluxConfig;
import io.micrometer.influx.InfluxMeterRegistry;

//import java.io.File;
import java.time.Duration;

public class InternalMonitoring {
    private static InfluxMeterRegistry registry;

    InternalMonitoring (InfluxDbConfiguration influxDbConfiguration) {
        InfluxConfig config = new InfluxConfig() {
            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }

            @Override
            public String uri() {
                return influxDbConfiguration.getInfluxdbUrl();
            }

            @Override
            public String db() {
                return influxDbConfiguration.getInfluxdbDb();
            }

            @Override
            public String get(String k) {
                return null;
            }
        };

        registry = new InfluxMeterRegistry(config, Clock.SYSTEM);

        registry.config().commonTags("application", "JVMThreadStateRecorder");

//        new ClassLoaderMetrics().bindTo(getRegistry());
//        new JvmMemoryMetrics().bindTo(getRegistry());
//        new JvmGcMetrics().bindTo(getRegistry());
//        new JvmThreadMetrics().bindTo(getRegistry());
//        new ExecutorServiceMetrics().bindTo(registry);
//        new JvmCompilationMetrics().bindTo(getRegistry());
//        new JvmHeapPressureMetrics().bindTo(getRegistry());
//        new JvmInfoMetrics().bindTo(getRegistry());
//        new UptimeMetrics().bindTo(getRegistry());
//        new ProcessorMetrics().bindTo(getRegistry());
//        new DiskSpaceMetrics(new File(System.getProperty("user.dir"))).bindTo(getRegistry());
//        new FileDescriptorMetrics().bindTo(getRegistry());
    }

    public static InfluxMeterRegistry getRegistry() {
        return registry;
    }
}
