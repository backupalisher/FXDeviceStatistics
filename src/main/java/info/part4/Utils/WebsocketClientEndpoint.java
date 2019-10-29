package info.part4.Utils;

import org.json.simple.parser.ParseException;

import javax.websocket.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

@ClientEndpoint
public class WebsocketClientEndpoint {
    private Session userSession;
    private MessageHandler messageHandler;

    static String SESSION_STATUS;


    public WebsocketClientEndpoint(URI endpointURI) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void uSession(String s){
        SESSION_STATUS = s;
    }
    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        System.out.println("opening websocket");
        this.userSession = userSession;
        uSession("open");

    }

    /**
     * Callback hook for Connection close events.
     *
     */
    @OnClose
    public void onClose() {
        System.out.println("closing websocket");
        this.userSession = null;
        uSession("close");
//        Thread.sleep(1000);
//        onOpen(new URI(Controller.URL_CLIENT_ENDPOINT));
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        if (this.messageHandler != null) {
            try {
                this.messageHandler.handleMessage(message);
            } catch (ParseException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                    InstantiationException | InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * register message handler
     *
     * @param msgHandler
     */
    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    /**
     * Send a message.
     *
     * @param message
     */
    public void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }

    /**
     * Message handler.
     *
     * @author Jiji_Sasidharan
     */
    public interface MessageHandler {
        void handleMessage(String message) throws ParseException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException, InterruptedException;
    }
}
