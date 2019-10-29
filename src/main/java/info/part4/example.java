package info.part4;

import com.sun.security.ntlm.Client;
import info.part4.Utils.ListenerWebsocketSessionStatus;
import info.part4.Utils.WebsocketClientEndpoint;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.websocket.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Timer;

import static info.part4.Controller.URL_CLIENT_ENDPOINT;

@ClientEndpoint
public class example {
    public static WebsocketClientEndpoint WEBSOCKET;
    // Один объект сканнер на всю программу, который закрываем перед выходом по EXIT
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
//        ListenerWebsocketSessionStatus listenerWebsocketSessionStatus = new ListenerWebsocketSessionStatus();
//        Timer timer = new Timer(true);
//        // будем запускать каждых 10 секунд (10 * 1000 миллисекунд)
//        timer.scheduleAtFixedRate(listenerWebsocketSessionStatus, 5000, 5 * 1000);

        initClientEnd();
//            SocketListener();

        System.out.println("Do you want to create a new worker? Enter YES to continue or EXIT to exit");
        if ("exit".equalsIgnoreCase(sc.nextLine())) {
            sc.close();
            System.exit(0);
        }
    }

    //Open WebSocket
    public static void initClientEnd() {
        WebSocketContainer container;

        container = ContainerProvider.getWebSocketContainer();
        try {
            Session session = container.connectToServer(Client.class, URI.create(Controller.URL_CLIENT_ENDPOINT));
        } catch (DeploymentException | IOException e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session p) {
        try {
            for (int i = 0; i < 10; i++) {
                p.getBasicRemote().sendText("Hello! ");

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            p.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void OnClose() {
        System.out.println("Client connection closed");
    }

}