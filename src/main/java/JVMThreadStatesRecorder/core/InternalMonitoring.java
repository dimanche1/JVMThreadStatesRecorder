package JVMThreadStatesRecorder.core;

import JVMThreadStatesRecorder.configuration.InfluxDbConfiguration;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.influx.InfluxConfig;
import io.micrometer.influx.InfluxMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import java.time.Duration;

public class InternalMonitoring {
    private static InfluxMeterRegistry influxMeterRegistry;
    private static PrometheusMeterRegistry prometheusMeterRegistry;
    private static InfluxDbConfiguration influxDbConfiguration;

    InternalMonitoring(InfluxDbConfiguration influxDbConfiguration) {

        this.influxDbConfiguration = influxDbConfiguration;

//        for (Connector connector : App.getConnectors()) {
//            connector.addBean(new JettyConnectionMetrics(influxMeterRegistry, connector));
//        }
    }

    public static void configureInfluxMeterRegistry() {
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

        influxMeterRegistry = new InfluxMeterRegistry(config, Clock.SYSTEM);

        influxMeterRegistry.config().commonTags("application", "JVMThreadStateRecorder");
    }

    public static void configurePrometheusMeterRegistry() {
        prometheusMeterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        prometheusMeterRegistry.config().commonTags("application", "JVMThreadStateRecorder");

        new ClassLoaderMetrics().bindTo(prometheusMeterRegistry);
        new JvmMemoryMetrics().bindTo(prometheusMeterRegistry);
        new JvmGcMetrics().bindTo(prometheusMeterRegistry);
        new JvmThreadMetrics().bindTo(prometheusMeterRegistry);
//        new ExecutorServiceMetrics().bindTo(prometheusMeterRegistry);
        new JvmCompilationMetrics().bindTo(prometheusMeterRegistry);
        new JvmHeapPressureMetrics().bindTo(prometheusMeterRegistry);
        new JvmInfoMetrics().bindTo(prometheusMeterRegistry);
        new UptimeMetrics().bindTo(prometheusMeterRegistry);
        new ProcessorMetrics().bindTo(prometheusMeterRegistry);
//        new DiskSpaceMetrics(new File(System.getProperty("user.dir"))).bindTo(prometheusMeterRegistry);
        new FileDescriptorMetrics().bindTo(prometheusMeterRegistry);
    }

    public static InfluxMeterRegistry getInfluxMeterRegistry() {
        return influxMeterRegistry;
    }

    public static PrometheusMeterRegistry getPrometheusMeterRegistry() {
        return prometheusMeterRegistry;
    }
}
