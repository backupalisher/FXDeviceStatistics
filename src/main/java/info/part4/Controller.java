package info.part4;

import info.part4.Utils.DynamicClassOverloader;
import info.part4.Utils.LoadSettings;
import info.part4.Utils.NotConnectedJson;
import info.part4.Utils.PingHost;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Controller implements Initializable {
    public static String URL_CLIENT_ENDPOINT;
    //    public static int USER_ID = 3;
    public static int COMPANY_ID;
    private static int DEVICE_ID;
    private static final String EVENT_PUT = "put";
    private static final String EVENT_GET = "get";

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

        LoadSettings.settings();

        final Socket socket;
        try {
            socket = IO.socket(URL_CLIENT_ENDPOINT, opts);
            System.out.println(URL_CLIENT_ENDPOINT);
            socket.on(Socket.EVENT_CONNECT, objects -> {
                System.out.println("Connect");
                TermAppend("Connect");
                socket.emit(EVENT_PUT, "Connect, user id: " + COMPANY_ID);
            }).on(EVENT_GET, objects -> {
                LoadSettings.settings();
                System.out.println("Message Received: ");
                TermAppend(objects[0].toString());
                for (Object arg : objects) {
                    String message = (String) arg;
                    System.out.println(message);

                    org.json.JSONObject jsonMessage;
                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = (JSONObject) jsonParser.parse(message);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    assert jsonObject != null;
                    JSONObject statusObject = (JSONObject) jsonObject.get("status");

                    if (jsonObject.toJSONString().contains("result")) {
                        System.out.println(statusObject.get("result").toString());
                        TermAppend(statusObject.get("result").toString());
                    } else if (jsonObject.toString().contains("getDevices")) {
                        String server_init = jsonObject.get("server_init").toString();
                        int company_id = Integer.parseInt(jsonObject.get("company_id").toString());

                        if (server_init.contains("getDevices")) {
                            if ((company_id == COMPANY_ID) || (company_id == 0)) {
                                JSONArray devices = (JSONArray) jsonObject.get("devices");

                                TermAppend("get device information");

                                System.out.println(jsonObject.toString());

                                for (Object device1 : devices) {
                                    JSONObject device = (JSONObject) device1;

                                    System.out.println(device.get("productname").toString());

                                    String productName = device.get("productname").toString();
                                    String device_url = device.get("url").toString();
                                    DEVICE_ID = Integer.parseInt(device.get("id").toString());

                                    PingHost pingHost = new PingHost();
                                    boolean device_online;
//                                    device_online = true;
                                    device_online = pingHost.ping(getIP(device_url), 80, 2000);
                                    System.out.println(device_url);
                                    if (device_online) {
                                        String name = productName.replaceAll("\\s+", "");
                                        try {
                                            ClassLoader loader = new DynamicClassOverloader(new String[]{"./modules"});
                                            Class clazz = Class.forName(name, true, loader);

                                            Class[] params = {String.class};
                                            Method method = clazz.getDeclaredMethod("parser", params);
                                            method.setAccessible(true);
                                            Object[] objectsJson = new Object[]{device_url};
                                            jsonMessage = (org.json.JSONObject) method.invoke(clazz.newInstance(), objectsJson);

//                                            System.out.println(jsonMessage.toString());

                                            jsonMessage.put("device_id", DEVICE_ID);
                                            jsonMessage.put("company_id", COMPANY_ID);

                                            socket.emit(EVENT_PUT, jsonMessage.toString());
                                            TermAppend(jsonMessage.toString());
                                            System.out.println(jsonMessage.toString());

                                        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        NotConnectedJson notConnectedJson = new NotConnectedJson();
                                        try {
                                            String notConnectDevice = notConnectedJson.errorJson(DEVICE_ID, "Нет связи с устройством").toString();
                                            socket.emit(EVENT_PUT, notConnectDevice);
                                            TermAppend(notConnectDevice);
                                            System.out.println(notConnectDevice);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                //Send command message
                sendButton.setOnAction(event -> {
                    socket.emit(EVENT_PUT, cmdEdit.getText());
                    TermAppend(cmdEdit.getText());
                    cmdEdit.setText("");
                });
            }).on(Socket.EVENT_DISCONNECT, objects -> {
                System.out.println("Client disconnected");
                TermAppend("Client disconnected");
            }).on(Socket.EVENT_CONNECT_ERROR, objects -> {
                Exception e = (Exception) objects[0];
                e.printStackTrace();
            }).on(Socket.EVENT_ERROR, objects -> {
                Exception e = (Exception) objects[0];
                e.printStackTrace();
//                TextArea(e.printStackTrace());
            }).on(Socket.EVENT_RECONNECT, objects -> {
                System.out.println("Reconnecting: ");
                TermAppend("Reconnecting...");
                for (Object arg : objects) {
                    System.out.println(arg);
                    TermAppend(arg.toString());
                }
            });
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void TermAppend(String message) {
        Date date = new Date();
        SimpleDateFormat moment;
        moment = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

        terminalText.appendText(moment.format(date) + " - " + message + "\r\n");
    }

    //get ip address from string
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
