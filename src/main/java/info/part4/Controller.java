package info.part4;

import info.part4.ParserModels.KyoceraM2540;
import info.part4.Utils.NotConnectedJson;
import info.part4.Utils.PingHost;
import info.part4.Utils.WebsocketClientEndpoint;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller implements Initializable {
    private static String URL_CLIENT_ENDPOINT = "ws://socket.api.part4.info:8080/";
    public static int USER_ID = 2;
    public static int COMPANY_ID = 1;
    public static int ADDRESS_ID = 1;
    public static String SERIAL_NUMBER;
    public static String DEVICE_NAME;
    public static int DEVICE_ID;

    //Open WebSocket
    final WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(new URI(URL_CLIENT_ENDPOINT));

    @FXML
    private TextArea terminalText;
    @FXML
    private TextField cmdEdit;
    @FXML
    private Button sendButton;

    public Controller() throws URISyntaxException {
    }

    public void SocketListener() {
        //SocketListener;
        clientEndPoint.addMessageHandler(message -> {
            terminalText.appendText(LocalDateTime.now() + ": " + message + "\r\n");
            JSONParser jsonParser = new JSONParser();
            JSONObject statusObject = (JSONObject) jsonParser.parse(message);
            JSONObject jsonObject = (JSONObject) statusObject.get("status");

            String server_init = jsonObject.get("server_init").toString();
            int init_client = Integer.parseInt(jsonObject.get("init_client").toString());

            if (server_init.contains("getInfo")) {
                if (init_client == 1) {

                    JSONArray devices = (JSONArray) jsonObject.get("devices");

                    Iterator i = devices.iterator();
                    // берем каждое значение из массива json отдельно
                    while (i.hasNext()) {
                        JSONObject device = (JSONObject) i.next();
                        String productName = device.get("productName").toString();
                        String device_url = device.get("url").toString();
                        String serialNumber = device.get("serialNumber").toString();
                        int device_id = Integer.parseInt(device.get("device_id").toString());

                        PingHost pingHost = new PingHost();
                        device_url = getIP(device_url);
                        boolean device_online;
                        device_online = pingHost.ping(device_url, 80, 2000);

                        if (device_online) {
                            String name = productName.replaceAll("\\s+", "");
                            System.out.println(name);
                            Class<?> c = Class.forName("info.part4.ParserModels." + name);
                            Class[] params = {String.class};

                            Method method = c.getDeclaredMethod("parser", params);
                            method.setAccessible(true);
                            Object[] objects = new Object[]{new String(device_url)};
                            String b = (String) method.invoke(c.newInstance(), objects);

                            System.out.println(b);
                        } else {
                            NotConnectedJson notConnectedJson = new NotConnectedJson();
                            notConnectedJson.errorJson(init_client, device_id, device_url);
                        }

                    }

//                        FileInputStream fileInputStream = new FileInputStream("c:\\1\\devicelist");
//                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
//                        String strLine;
//                        String[] subStr;
//                        while ((strLine = bufferedReader.readLine()) != null) {
//                            subStr = strLine.split(";");
//                            switch (subStr[0]) {
////                                    case "HP LaserJet 600 M603":
////                                        clientEndPoint.sendMessage(parseM603(subStr[1]));
////                                        break;
////                                    case "HP LaserJet 500 MFP M525":
////                                        clientEndPoint.sendMessage(parseM525(subStr[1]));
////                                        break;
////                                case "Brother MFC-L2700DN":
////                                    clientEndPoint.sendMessage(parserL2700(subStr[1]));
////                                    break;
//                                case "Kyocera ECOSYS M2540dn":
//                                    DEVICE_NAME = subStr[0];
//                                    SERIAL_NUMBER = subStr[2];
//                                    KyoceraM2540 kyoceraM2540 = new KyoceraM2540();
//                                    clientEndPoint.sendMessage(kyoceraM2540.parser(subStr[1]));
//                                    break;
//                            }
//                        }
                }
            }
        });
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

//    private static String getBrand() throws KeyManagementException, NoSuchAlgorithmException {
//        String jsonMessage = null;
//
//        String url = BASE_PATH;
//        Document page;
//
//
//            page = getPage(url);
//
//            Element status = page.select("span[id=MachineStatus]").first();
//            Element cartridge = page.select("span[id=SupplyGauge0]").first();
//            Element KIT = page.select("span[id=SupplyGauge1]").first();
//
//            Document configurationPage = getPage(BASE_PATH + "hp/device/InternalPages/Index?id=ConfigurationPage");
//            Element productName = configurationPage.select("strong[id=ProductName]").first();
//            Element serialNumber = configurationPage.select("strong[id=SerialNumber]").first();
//            Element maintenanceKitCount = configurationPage.select("strong[id=EngineMaintenanceKitCount]").first();
//            Element engineCycles = configurationPage.select("strong[id=EngineCycles]").first();
//
//            JSONObject obj = new JSONObject();
//            JSONArray arr;
//            JSONObject arr_obj;
//            try {
//                obj.put("init_client", USER_ID);
//                obj.put("company_id", COMPANY_ID);
//                obj.put("address_id", ADDRESS_ID);
//                obj.put("productName", productName.text());
//                obj.put("url", BASE_PATH);
//                obj.put("serialNumber", serialNumber.text());
//                obj.put("article", "0");
//                obj.put("client_article", "0");
//                obj.put("status", status.text());
//                obj.put("printCycles", engineCycles.text());
//
//                arr_obj = new JSONObject();
//                arr = new JSONArray(new ArrayList<String>());
//                arr_obj.put("maintenanceKitCount", maintenanceKitCount.text());
//                arr.put(arr_obj);
//
////            arr_obj = new JSONObject();
////            arr_obj.put("Transfer belt", 123);
////            arr.put(arr_obj);
//                obj.put("KIT", (Object) arr);
//
//                arr_obj = new JSONObject();
//                arr = new JSONArray(new ArrayList<String>());
//                arr_obj.put("black", cartridge.text());
//                arr.put(arr_obj);
//
////            arr_obj = new JSONObject();
////            arr_obj.put("cyan", 60);
////            arr.put(arr_obj);
////
////            arr_obj = new JSONObject();
////            arr_obj.put("magente", 35);
////            arr.put(arr_obj);
////
////            arr_obj = new JSONObject();
////            arr_obj.put("yellow", 100);
////            arr.put(arr_obj);
//                obj.put("cartridge", (Object) arr);
//
////            arr_obj = new JSONObject();
////            arr = new JSONArray(new ArrayList<String>());
////            arr_obj.put("date", "24.09.2019");
////            arr_obj.put("info", "low cartridge");
////            arr.put(arr_obj);
////
////            arr_obj = new JSONObject();
////            arr_obj.put("date", "25.09.2019");
////            arr_obj.put("error", "low cartridge");
////            arr.put(arr_obj);
////            obj.put("log", (Object) arr);
//
//                jsonMessage = obj.toString();
//                System.out.println(jsonMessage);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            System.out.println(jsonMessage);
//        } else jsonMessage = "Нет связи с устройством, по адрусу: " + BASE_PATH;
//        return jsonMessage;
//    }

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SocketListener();
        sendButton.setOnAction(event -> clientEndPoint.sendMessage(cmdEdit.getText()));
    }
}
