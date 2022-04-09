package JVMThreadStatesRecorder;

import JVMThreadStatesRecorder.Configuration.Configuration;
import JVMThreadStatesRecorder.Configuration.InfluxDbConfiguration;
import JVMThreadStatesRecorder.Core.InternalMonitoring;
import JVMThreadStatesRecorder.Core.JVMThreadStateRecorder;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.HttpCode;
import org.eclipse.jetty.server.Connector;

public class App {
    private static Javalin app;
    private static JVMThreadStateRecorder jvmThreadStateRecorder = new JVMThreadStateRecorder();
    private static Connector[] connectors;

    public static void main(String[] args) {

        int port = handleCommandArgs(args);

        // Gracefully shutting the JVMThreadStateRecorder down
        Thread shutDownHookThread = new Thread(() -> {
            jvmThreadStateRecorder.closeConnectionInfluxDB();
            jvmThreadStateRecorder.stopAllTasks();
        });
        Runtime.getRuntime().addShutdownHook(shutDownHookThread);

        app = Javalin.create().start(port);

        app.post("/influxdb-configuration", ctx -> {
            ctx.result(jvmThreadStateRecorder.influxDbConnect(ctx.bodyAsClass(InfluxDbConfiguration.class)));
        });

        app.post("/start", ctx -> {
            int jvmThreadStateRecorderID = jvmThreadStateRecorder.start(ctx.bodyAsClass(Configuration.class));

            ctx.result(String.valueOf(jvmThreadStateRecorderID));
        });

        app.get("/stop/id={id}", ctx -> {
            String response = jvmThreadStateRecorder.stop(Integer.parseInt(ctx.pathParam("id")));

            ctx
                .status(HttpCode.OK)
                .contentType(ContentType.PLAIN)
                .result(response);
        });

        app.get("/tasks", ctx -> ctx
                .status(HttpCode.OK)
                .contentType(ContentType.APPLICATION_JSON)
                .result(jvmThreadStateRecorder.tasks()));

        app.get("/dbconfig", ctx -> ctx.result(jvmThreadStateRecorder.getDbConfig()));

        app.get("/internal-monitoring", ctx -> {
            String result = jvmThreadStateRecorder.internalMonitoring();
            switch (result) {
                case "Error: InfluxDB unavailable": ctx.status(HttpCode.SERVICE_UNAVAILABLE)
                        .contentType(ContentType.PLAIN)
                        .result(result);
                    break;
                case "Error: Couldn't start internal monitoring": ctx.status(HttpCode.SERVICE_UNAVAILABLE)
                        .contentType(ContentType.PLAIN)
                        .result(result);
                    break;
                case "Internal monitoring started": ctx.status(HttpCode.OK)
                        .contentType(ContentType.PLAIN)
                        .result("Internal monitoring started");
                    break;
                default: ctx.status(HttpCode.SERVICE_UNAVAILABLE)
                        .contentType(ContentType.PLAIN)
                        .result("Error: Something wrong");
                    break;
            }
        });
    }

//        app.get("/internalMonitoringJvm", ctx -> {
//            boolean result = jvmThreadStateRecorder.internalMonitoringJvm();
//            if (result) {
//                ctx
//                        .status(HttpCode.OK)
//                        .contentType(ContentType.PLAIN)
//                        .result("JVM internal monitoring started.");
//
//            } else {
//                ctx.status(HttpCode.INTERNAL_SERVER_ERROR);
//            }
//        });
//
//        app.get("/getJvmStats", ctx -> {
//            ctx
//                    .status(HttpCode.OK)
////                    .contentType(ContentType.)
//                    .result(InternalMonitoring.getPrometheusMeterRegistry().scrape());
//        });
//    }

    private static int handleCommandArgs(String[] args) {
        int port = 7070;

        switch (args.length) {
            case 1: port = Integer.parseInt(args[0]);
                break;
            case 3: port = Integer.parseInt(args[0]);
                InfluxDbConfiguration influxDbConfiguration = new InfluxDbConfiguration();
                influxDbConfiguration.setInfluxdbUrl(args[1]);
                influxDbConfiguration.setInfluxdbDb(args[2]);
                System.out.println(jvmThreadStateRecorder.influxDbConnect(influxDbConfiguration));
                break;
            default: break;
        }
//        Configuration.InfluxDbConfiguration influxDbConfiguration = new Configuration.InfluxDbConfiguration();
//        port = Integer.parseInt(System.getProperty("JavalinPort"));
//        influxDbConfiguration.setInfluxdbUrl(System.getProperty("InfluxdbUrl"));
//        influxDbConfiguration.setInfluxdbDb(System.getProperty("InfluxdbDb"));

        return port;
    }

    public static Connector[] getConnectors() {
        return  app.jettyServer().server().getConnectors();
    }
}
