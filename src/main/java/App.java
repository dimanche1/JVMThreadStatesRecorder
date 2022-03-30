import Configuration.*;

import Core.JVMThreadStateRecorder;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.HttpCode;

public class App {
    private static JVMThreadStateRecorder jvmThreadStateRecorder = new JVMThreadStateRecorder();

    public static void main(String[] args) {

        int port = handleCommandArgs(args);

        Javalin app = Javalin.create().start(port);

        app.post("/Configuration.InfluxDbConfiguration", ctx -> {
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

        app.get("/internalMonitoring", ctx -> {
            boolean result = jvmThreadStateRecorder.internalMonitoring();
            if (result) {
                ctx
                    .status(HttpCode.OK)
                        .contentType(ContentType.PLAIN)
                        .result("Internal monitoring started.");

            } else {
                ctx.status(HttpCode.INTERNAL_SERVER_ERROR);
            }
        });
    }

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
}
