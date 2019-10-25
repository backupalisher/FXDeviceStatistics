package info.part4;

import info.part4.ParserModels.KyoceraM2540;
import info.part4.Utils.WebsocketClientEndpoint;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public static int USER_ID = 2;
    public static int COMPANY_ID = 1;
    public static int ADDRESS_ID = 1;
    public static String SERIAL_NUMBER;
    public static String DEVICE_NAME;

    //Open WebSocket
    final WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(new URI("ws://socket.api.part4.info:8080/"));

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
            if (message.contains("getDevices")) {
                if (message.contains("2")) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream("c:\\1\\devicelist");
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                        String strLine;
                        String[] subStr;
                        while ((strLine = bufferedReader.readLine()) != null) {
                            subStr = strLine.split(";");
                            switch (subStr[0]) {
//                                    case "HP LaserJet 600 M603":
//                                        clientEndPoint.sendMessage(parseM603(subStr[1]));
//                                        break;
//                                    case "HP LaserJet 500 MFP M525":
//                                        clientEndPoint.sendMessage(parseM525(subStr[1]));
//                                        break;
//                                case "Brother MFC-L2700DN":
//                                    clientEndPoint.sendMessage(parserL2700(subStr[1]));
//                                    break;
                                case "Kyocera ECOSYS M2540dn":
                                    DEVICE_NAME = subStr[0];
                                    SERIAL_NUMBER = subStr[2];
                                    KyoceraM2540 kyoceraM2540 = new KyoceraM2540();
                                    clientEndPoint.sendMessage(kyoceraM2540.parser(subStr[1]));
                                    break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


//    private static String getBrand() throws KeyManagementException, NoSuchAlgorithmException {
//        String jsonMessage = null;
//
//        String url = BASE_PATH;
//        Document page;
//
//        if (pingHost(BASE_PATH, 80, 2000)) {
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
