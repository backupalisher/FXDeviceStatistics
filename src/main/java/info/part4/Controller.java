package info.part4;


import info.part4.Utils.NotConnectedJson;
import info.part4.Utils.PingHost;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Controller implements Initializable {
    public static int USER_ID = 1;
    public static int COMPANY_ID = 1;
    public static int ADDRESS_ID = 1;

    @FXML
    private TextArea terminalText;
    @FXML
    private TextField cmdEdit;
    @FXML
    private Button sendButton;

    public Controller() {
    }


    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        IO.Options opts = new IO.Options();
        opts.forceNew = true;
        opts.reconnection = true;

        final Socket socket;
        try {
            String URL_CLIENT_ENDPOINT = "http://socket.api.part4.info:3000";
            socket = IO.socket(URL_CLIENT_ENDPOINT, opts);
            socket.on(Socket.EVENT_CONNECT, args -> {
                socket.emit("message", "Connect, user id: " + USER_ID);
//                System.out.println(args[0]);
//                JSONObject object = new JSONObject();
//                try {
//                    object.put("sessionToken", "1d0bbced4d560af3ae22bc4513bfa400");
//                    socket.emit("echo", object.toString());
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
            }).on("chat message", objects -> {
                System.out.println(objects[0]);
                terminalText.appendText(objects[0].toString()+"\r\n");
            }).on(Socket.EVENT_MESSAGE, args -> {
                System.out.println("Message Received: ");
                for (Object arg : args) {
                    System.out.println(arg);
                    terminalText.appendText(arg.toString() + "\r\n");
                    String message = (String) args[0];
                    if (message.contains("getInfo")) {

                        JSONParser jsonParser = new JSONParser();
                        JSONObject statusObject = null;
                        try {
                            statusObject = (JSONObject) jsonParser.parse(message);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        JSONObject jsonObject = (JSONObject) statusObject.get("status");

                        String server_init = jsonObject.get("server_init").toString();
                        int init_client = Integer.parseInt(jsonObject.get("init_client").toString());

                        if (server_init.contains("getInfo")) {
                            if (init_client == USER_ID) {
                                JSONArray devices = (JSONArray) jsonObject.get("devices");

                                Iterator j = devices.iterator();
                                // берем каждое значение из массива json отдельно
                                while (j.hasNext()) {
                                    JSONObject device = (JSONObject) j.next();
                                    String productName = device.get("productName").toString();
                                    String device_url = device.get("url").toString();
                                    int device_id = Integer.parseInt(device.get("device_id").toString());

                                    PingHost pingHost = new PingHost();
                                    boolean device_online;
                                    device_online = pingHost.ping(getIP(device_url), 80, 2000);

                                    if (device_online) {
                                        String name = productName.replaceAll("\\s+", "");
                                        try {
                                            Class<?> clazz = Class.forName("info.part4.ParserModels." + name);
                                            Class[] params = {String.class};

                                            Method method = clazz.getDeclaredMethod("parser", params);
                                            method.setAccessible(true);
                                            Object[] objectsJson = new Object[]{device_url};
                                            String jsonMessage = (String) method.invoke(clazz.newInstance(), objectsJson);
                                            socket.emit("message", jsonMessage);
                                            terminalText.appendText(jsonMessage + "\r\n");
                                            System.out.println(jsonMessage);

                                        } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        NotConnectedJson notConnectedJson = new NotConnectedJson();
                                        try {
                                            socket.emit("message", notConnectedJson.errorJson(init_client, device_id, device_url));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }).on(Socket.EVENT_DISCONNECT, args -> System.out.println("Client disconnected")).on(Socket.EVENT_CONNECT_ERROR, args -> {
                Exception e = (Exception) args[0];
                e.printStackTrace();
            }).on(Socket.EVENT_ERROR, args -> {
                Exception e = (Exception) args[0];
                e.printStackTrace();
            }).on(Socket.EVENT_RECONNECT, args -> {
                System.out.println("Reconnecting: ");
                for (Object arg : args) {
                    System.out.println(arg);
                    terminalText.appendText(arg.toString() + "\r\n");
                }
            });
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        sendButton.setOnAction(event -> cmdEdit.setText(""));
    }

    private String getIP(String... lines) {
        String ip = null;
        Pattern p = Pattern.compile("(\\d{0,3}\\.){3}\\d{0,3}");
        for (String s : lines) {
            Matcher m = p.matcher(s);
            if (m.find())
                ip = m.group();
        }
        return ip;
    }
}
