import io.javalin.Javalin;

public class App {
    private static JVMThreadStateRecorder jvmThreadStateRecorder = new JVMThreadStateRecorder();

    public static void main(String[] args) {
        int port = 7070;

        switch (args.length) {
            case 1: port = Integer.parseInt(args[0]);
                    break;
            case 3: port = Integer.parseInt(args[0]);
                    InfluxDbConfiguration influxDbConfiguration = new InfluxDbConfiguration();
                    influxDbConfiguration.setInfluxdbUrl(args[1]);
                    influxDbConfiguration.setInfluxdbDb(args[2]);
                    System.out.println(jvmThreadStateRecorder.dbConfiguration(influxDbConfiguration));
                    break;
            default: break;
        }
//        InfluxDbConfiguration influxDbConfiguration = new InfluxDbConfiguration();
//        port = Integer.parseInt(System.getProperty("JavalinPort"));
//        influxDbConfiguration.setInfluxdbUrl(System.getProperty("InfluxdbUrl"));
//        influxDbConfiguration.setInfluxdbDb(System.getProperty("InfluxdbDb"));

        Javalin app = Javalin.create().start(port);

        app.post("/InfluxDbConfiguration", ctx -> {
            ctx.result(jvmThreadStateRecorder.dbConfiguration(ctx.bodyAsClass(InfluxDbConfiguration.class)));
        });

        app.post("/start", ctx -> {
            int jvmThreadStateRecorderID = jvmThreadStateRecorder.start(ctx.bodyAsClass(Configuration.class));

            ctx.result(String.valueOf(jvmThreadStateRecorderID));
        });

        app.get("/stop/id={id}", ctx -> {
            String response = jvmThreadStateRecorder.stop(Integer.parseInt(ctx.pathParam("id")));

            ctx.result(response);
        });
    }
}
