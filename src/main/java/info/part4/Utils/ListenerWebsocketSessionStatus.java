package info.part4.Utils;

import info.part4.Controller;
import java.net.URISyntaxException;
import java.util.TimerTask;

public class ListenerWebsocketSessionStatus extends TimerTask {
    @Override
    public void run() {
        System.out.println("Websocket status: " + WebsocketClientEndpoint.SESSION_STATUS);
        if (WebsocketClientEndpoint.SESSION_STATUS.contains("close")) {
            PingHost pingHost = new PingHost();
            boolean b = pingHost.ping("socket.api.part4.info", 80, 1000);
            if (b) {
                try {
                    Controller.WEBSOCKET = Controller.initClientEnd(Controller.URL_CLIENT_ENDPOINT);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
//                example.initClientEnd();
            }
        }
    }

//    private void init() throws IOException, URISyntaxException {
//
//        final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
//        final File currentJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
//
//        /* is it a jar file? */
//        if (!currentJar.getName().endsWith(".jar"))
//            return;
//
//        /* Build command: java -jar application.jar */
//        final ArrayList<String> command = new ArrayList<>();
//        command.add(javaBin);
//        command.add("-jar");
//        command.add(currentJar.getPath());
//
//        System.out.println(command);
//
//        final ProcessBuilder builder = new ProcessBuilder(command);
//        builder.start();
//        System.out.println("started");
//        System.exit(0);
//    }
}
