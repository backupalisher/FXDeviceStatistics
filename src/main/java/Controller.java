import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.*;
import javax.print.DocFlavor;
import java.io.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private static int USER_ID = 2;
    private static int COMPANY_ID = 1;
    private static int ADDRESS_ID = 1;
    private static String SERIAL_NUMBER;
    private static String DEVICE_NAME;

    String dd; 
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
                                case "Kyocera ECOSYS M2030dn":
                                    DEVICE_NAME = subStr[0];
                                    SERIAL_NUMBER = subStr[2];
                                    clientEndPoint.sendMessage(parserM2030(subStr[1]));
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

    public static boolean pingHost(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }

    public static Document getPage(String link) throws NoSuchAlgorithmException, KeyManagementException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        String html = null;
        try {
            URL url = new URL(link);
            URLConnection con = url.openConnection();
            con.setConnectTimeout(2000);
            Reader reader = new InputStreamReader(con.getInputStream());
            if (reader != null) {
                while (true) {
                    int ch = reader.read();
                    if (ch == -1) {
                        break;
                    }
                    html += String.valueOf((char) ch);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (html != null) {
            Document page = Jsoup.parse(html);
            return page;
        }
//        System.out.println(page);
//        Document page = Jsoup.connect(link)
//                .timeout(1000).validateTLSCertificates(false).get();
        return null;
    }

    //HP LaserJet 600 M603
    private static String parserM603(String url) throws KeyManagementException, NoSuchAlgorithmException {
        String jsonMessage = null;
        Document page;

//        if (pingHost(url, 80, 2000)) {
        page = getPage(url);

        if (page != null) {
            Element status = page.select("span[id=MachineStatus]").first();
            Element cartridge = page.select("span[id=SupplyGauge0]").first();
            Element KIT = page.select("span[id=SupplyGauge1]").first();

            Document configurationPage = getPage(url + "/hp/device/InternalPages/Index?id=ConfigurationPage");
            Element productName = configurationPage.select("strong[id=ProductName]").first();
            Element serialNumber = configurationPage.select("strong[id=SerialNumber]").first();
            Element maintenanceKitCount = configurationPage.select("strong[id=EngineMaintenanceKitCount]").first();
            Element engineCycles = configurationPage.select("strong[id=EngineCycles]").first();

            JSONObject obj = new JSONObject();
            JSONArray arr;
            JSONObject arr_obj;
            try {
                obj.put("init_client", USER_ID);
                obj.put("company_id", COMPANY_ID);
                obj.put("address_id", ADDRESS_ID);
                obj.put("productName", productName.text());
                obj.put("url", url);
                obj.put("serialNumber", serialNumber.text());
                obj.put("article", "0");
                obj.put("client_article", "0");
                obj.put("status", status.text());
                obj.put("printCycles", engineCycles.text());

                arr_obj = new JSONObject();
                arr = new JSONArray(new ArrayList<String>());
                arr_obj.put("maintenanceKitCount", maintenanceKitCount.text());
                arr.put(arr_obj);
                obj.put("KIT", (Object) arr);

                arr_obj = new JSONObject();
                arr = new JSONArray(new ArrayList<String>());
                arr_obj.put("black", cartridge.text());
                arr.put(arr_obj);
                obj.put("cartridge", (Object) arr);

                jsonMessage = obj.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return errorJson(url);
        }
        return jsonMessage;
    }

    //HP LaserJet 500 MFP M525
    private static String parserM525(String url) throws KeyManagementException, NoSuchAlgorithmException {
        String jsonMessage = null;
        Document page;

        page = getPage(url);

        if (page != null) {
            Element status = page.select("span[id=MachineStatus]").first();
            Element cartridge = page.select("span[id=SupplyGauge0]").first();

            //https://192.168.1.233/hp/device/InternalPages/Index?id=ConfigurationPage
            Document configurationPage = getPage(url + "/hp/device/InternalPages/Index?id=ConfigurationPage");
            Element productName = configurationPage.select("strong[id=ProductName]").first();
            Element serialNumber = configurationPage.select("strong[id=SerialNumber]").first();

            Element adfCycles = configurationPage.select("strong[id=ADFMaintenance]").first();
            Element engineCycles = configurationPage.select("strong[id=EngineCycles]").first();

            JSONObject obj = new JSONObject();
            JSONArray arr;
            JSONObject arr_obj;
            try {
                obj.put("init_client", USER_ID);
                obj.put("company_id", COMPANY_ID);
                obj.put("address_id", ADDRESS_ID);
                obj.put("productName", productName.text());
                obj.put("url", url);
                obj.put("serialNumber", serialNumber.text());
                obj.put("article", "0");
                obj.put("client_article", "0");
                obj.put("status", status.text());
                obj.put("printCycles", engineCycles.text());
                arr_obj = new JSONObject();
                arr = new JSONArray(new ArrayList<String>());

                arr_obj.put("adfCycles", adfCycles.text());
                arr.put(arr_obj);
                obj.put("KIT", (Object) arr);
                arr_obj = new JSONObject();
                arr = new JSONArray(new ArrayList<String>());
                arr_obj.put("black", cartridge.text());
                arr.put(arr_obj);
                obj.put("cartridge", (Object) arr);
                jsonMessage = obj.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return errorJson(url);
        }
        return jsonMessage;
    }

    private static String errorJson(String url) {
        String jsonMessage;
        JSONObject obj = new JSONObject();
        //{"init_client_error": 1, "url": "https://192.168.1.233", "error": "Нет связи с устройством, по адресу: https://192.168.1.233"}
        obj.put("init_client_error", USER_ID);
        obj.put("serialNumber",SERIAL_NUMBER);
        obj.put("productName",DEVICE_NAME);
        obj.put("error", "Нет связи с устройством, по адресу: " + url);
        jsonMessage = obj.toString();
        System.out.println(jsonMessage);
        return jsonMessage;
    }

    //Kyocera ECOSYS M2030dn
    private static String parserM2030(String url) {
        String body = null;
        try {
            HttpClient client = new DefaultHttpClient();

            HttpGet response = new HttpGet(url + "/js/jssrc/model/dvcinfo/dvccounter/DvcInfo_Counter_PrnCounter.model.htm");
            ResponseHandler<String> handler = new BasicResponseHandler();
            body = client.execute(response, handler);

            response = new HttpGet(url + "/js/jssrc/model/dvcinfo/dvccounter/DvcInfo_Counter_ScanCounter.model.htm");
            body += client.execute(response, handler);

            response = new HttpGet(url + "/js/jssrc/model/startwlm/Hme_DvcSts.model.htm");
            body += client.execute(response, handler);

            response = new HttpGet(url + "/js/jssrc/model/dvcinfo/dvcconfig/DvcConfig_Config.model.htm?arg1=0");
            body += client.execute(response, handler);

            response = new HttpGet(url + "/js/jssrc/model/startwlm/Hme_Toner.model.htm");
            body += client.execute(response, handler);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            return errorJson(url);
//            e.printStackTrace();
        }

        String[] subStr;
        int printTotal = 0;
        int scanTotal = 0;

        JSONObject obj = new JSONObject();
        JSONArray arr;
        JSONObject arr_obj = null;

        obj.put("init_client", USER_ID);
        obj.put("new",1);//1 - это новый, если 0 то это старый
        obj.put("url", url);
        obj.put("article", "0");
        obj.put("client_article", "0");

        subStr = body.split(";");
        for (int i = 1; i < subStr.length; i++) {
            if (subStr[i].contains("_pp.copytotal")) {
                printTotal = Integer.parseInt(subStr[i].replaceAll("[^0-9\\\\+]", ""));
            }
            if (subStr[i].contains("_pp.printertotal")) {
                printTotal += Integer.parseInt(subStr[i].replaceAll("[^0-9\\\\+]", ""));
                obj.put("printCycles", printTotal);
            }
            if (subStr[i].contains("_pp.scanCopy")) {
                scanTotal = Integer.parseInt(subStr[i].replaceAll(", 10", "").replaceAll("[^0-9\\\\+]", ""));
            }
            if (subStr[i].contains("_pp.scanBlackWhite")) {
                scanTotal = scanTotal + Integer.parseInt(subStr[i].replaceAll(", 10", "").replaceAll("[^0-9\\\\+]", ""));
            }
            if (subStr[i].contains("_pp.scanOther")) {
                scanTotal = scanTotal + Integer.parseInt(subStr[i].replaceAll(", 10", "").replaceAll("[^0-9\\\\+]", ""));
                obj.put("scanCycles", scanTotal);
            }
            if (subStr[i].contains("_pp.PanelMessage")) {
                obj.put("status", subStr[i].replaceAll("_pp.PanelMessage = '", "").replaceAll("'", "").trim());
            }
            if (subStr[i].contains("_pp.bonjourName")) {
                obj.put("productName", subStr[i].replaceAll("_pp.bonjourName = '", "").replaceAll("'", "").trim());
            }
            if (subStr[i].contains("_pp.serialNumber")) {
                obj.put("serialNumber", subStr[i].replaceAll("_pp.serialNumber = '", "").replaceAll("'", "").trim());
            }
            if (subStr[i].contains("_pp.ipv4IPAddressWired")) {
                System.out.println(subStr[i].replaceAll("_pp.ipv4IPAddressWired = '", "").replaceAll("'", "").trim());
            }
            if (subStr[i].contains("_pp.Renaming.push")) {
                if (!subStr[i].contains("_pp.Renaming.push(parseInt('-1', 10))")) {
                    arr_obj = new JSONObject();
                    arr = new JSONArray(new ArrayList<String>());
                    arr_obj.put("black", subStr[i].replaceAll(",10", "").replaceAll("[^0-9\\\\+]", ""));
                    arr.put(arr_obj);
                    obj.put("cartridge", (Object) arr);
                }
            }
        }
        System.out.println(obj.toString());
        return obj.toString();
    }

    //Brother MFC-L2700DN
    private static String parserL2700(String url) throws KeyManagementException, NoSuchAlgorithmException, IOException {
        String jsonMessage = null;
        Document page;
        page = getPage(url);

        if (page != null) {
            Elements contentsGroup = page.select("div[class=contentsGroup");

            JSONObject obj = new JSONObject();
            JSONArray arr;
            JSONObject arr_obj;
            obj.put("init_client", "1");
            obj.put("company_id", "1");
            obj.put("address_id", "1");
            obj.put("url", url);
            obj.put("status", "Готов");

            int i = 0;
            for (Element td : contentsGroup) {
                Elements dd = td.select("dd");
                for (Element s : dd) {
                    if (i == 0) {
                        obj.put("productName", s.text().replaceAll("series", "").trim());
                    }
                    if (i == 1) {
                        obj.put("serialNumber", s.text());
                    }
                    if (i == 4) {
                        obj.put("printCycles", s.text());
                    }
                    if (i == 5) {
                        arr_obj = new JSONObject();
                        arr = new JSONArray(new ArrayList<String>());
                        arr_obj.put("drumCycles", s.text());
                        arr.put(arr_obj);
                        obj.put("KIT", (Object) arr);
                    }
                    if (i == 6) {
                        arr_obj = new JSONObject();
                        arr = new JSONArray(new ArrayList<String>());
                        arr_obj.put("black", s.text());
                        arr.put(arr_obj);
                        obj.put("cartridge", (Object) arr);
                    }
                    i++;
                }
            }
            jsonMessage = obj.toString();
        } else {
            return errorJson(url);
        }
        System.out.println(jsonMessage);
        return jsonMessage;
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
        sendButton.setOnAction(event -> {
            clientEndPoint.sendMessage(cmdEdit.getText());
        });
    }
}
