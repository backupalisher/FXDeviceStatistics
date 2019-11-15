package info.part4;


import info.part4.Utils.LoadSettings;
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
    public static String URL_CLIENT_ENDPOINT = "http://socket.api.part4.info:3000";
    public static int USER_ID = 3;
    public static int COMPANY_ID = 26;
    public static int DEVICE_ID = 0;
    public static int ADDRESS_ID = 3;

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

        LoadSettings loadSettings = new LoadSettings();
        loadSettings.settings();

        IO.Options opts = new IO.Options();
        opts.forceNew = true;
        opts.reconnection = true;

        final Socket socket;
        try {

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
            }).on("message", objects -> {
                System.out.println(objects[0]);
                terminalText.appendText(objects[0].toString() + "\r\n");
            }).on(Socket.EVENT_MESSAGE, args -> {
                System.out.println("Message Received: ");
                for (Object arg : args) {
                    String message = (String) arg;
                    if (message.contains("status")) {
                        JSONParser jsonParser = new JSONParser();
                        JSONObject statusObject = null;
                        try {
                            statusObject = (JSONObject) jsonParser.parse(message);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        JSONObject jsonObject = (JSONObject) statusObject.get("status");

                        System.out.println(jsonObject);
                        if (jsonObject.toJSONString().contains("result")) {
                            System.out.println(jsonObject.get("result").toString());
                        } else {
                            String server_init = jsonObject.get("server_init").toString();
                            int company_id = Integer.parseInt(jsonObject.get("company_id").toString());

                            if (server_init.contains("getDevices")) {
                                if ((company_id == COMPANY_ID) || (company_id == 0)) {
                                    JSONArray devices = (JSONArray) jsonObject.get("devices");

                                    Iterator j = devices.iterator();
                                    // берем каждое значение из массива json отдельно
                                    while (j.hasNext()) {
                                        JSONObject device = (JSONObject) j.next();
                                        String productName = device.get("productName").toString();
                                        String device_url = device.get("url").toString();
                                        DEVICE_ID = Integer.parseInt(device.get("device_id").toString());

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
                                                socket.emit("message", notConnectedJson.errorJson(COMPANY_ID, DEVICE_ID));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                //Send command message
                sendButton.setOnAction(event -> {
                    socket.emit("message", cmdEdit.getText());
                    cmdEdit.setText("");
                });
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
