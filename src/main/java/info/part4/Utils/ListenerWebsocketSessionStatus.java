package info.part4.Utils;

import info.part4.Main;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.TimerTask;

public class ListenerWebsocketSessionStatus extends TimerTask {
    @Override
    public void run() {
//        System.out.println("Listener Websocket Status: " + WebsocketClientEndpoint.SESSION_STATUS);
//        if (WebsocketClientEndpoint.SESSION_STATUS.contains("CLOSE")) {
            PingHost pingHost = new PingHost();

            boolean b = pingHost.ping("socket.api.part4.info", 80, 1000);
            System.out.println(b);
            if (b) {
                try {
                    init();
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
//        }
    }

    private void init() throws IOException, URISyntaxException {

        final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        final File currentJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());

        /* is it a jar file? */
        if (!currentJar.getName().endsWith(".jar"))
            return;

        /* Build command: java -jar application.jar */
        final ArrayList<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-jar");
        command.add(currentJar.getPath());

        System.out.println(command);

        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.start();
        System.out.println("started");
        System.exit(0);
    }
}
