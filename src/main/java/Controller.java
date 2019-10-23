import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.*;
import javax.print.DocFlavor;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller {
    private static String BASE_PATH = "https://192.168.1.201/";
    private static int USER_ID = 1;
    private static int COMPANY_ID = 1;
    private static int ADDRESS_ID = 1;

    String dd; 
    //Open WebSocket
    final WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(new URI("ws://socket.api.part4.info:8080/"));

    @FXML
    private ResourceBundle resources;

    @FXML
    private DocFlavor.URL location;

    @FXML
    private TextArea terminalText;

    @FXML
    private TextField cmdEdit;

    @FXML
    private Button sendButton;

    public Controller() throws URISyntaxException {
    }

    @FXML
    void initialize() {
        SocketListener();

        sendButton.setOnAction(event -> {
            try {
                clientEndPoint.sendMessage(getBrand());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
        });
    }

    public void SocketListener() {
        //SocketListener;
        clientEndPoint.addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
            public void handleMessage(String message) {
                terminalText.appendText(message + "\r\n");
                if (message.contains("getDevices")) {
                    if (message.contains("5"))
                        sendButton.fire();
                }
            }
        });
    }

    public static Document getPage(String link) throws IOException, NoSuchAlgorithmException, KeyManagementException {
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

        URL url = new URL(link);
        URLConnection con = url.openConnection();
        Reader reader = new InputStreamReader(con.getInputStream());
        String html = null;
        while (true) {
            int ch = reader.read();
            if (ch == -1) {
                break;
            }
            html += String.valueOf((char) ch);
        }

//        System.out.println(html);
        Document page = Jsoup.parse(html);
//        System.out.println(page);
//        Document page = Jsoup.connect(link)
//                .timeout(1000).validateTLSCertificates(false).get();
        return page;
    }

    public static String getBrand() throws IOException, KeyManagementException, NoSuchAlgorithmException {
        String jsonMessage = null;

        String url = BASE_PATH;
        Document page;
        page = getPage(url);

        Element status = page.select("span[id=MachineStatus]").first();
        Element cartridge = page.select("span[id=SupplyGauge0]").first();
        Element KIT = page.select("span[id=SupplyGauge1]").first();

        Document configurationPage = getPage(BASE_PATH + "hp/device/InternalPages/Index?id=ConfigurationPage");
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
            obj.put("device", productName.text());
            obj.put("url", BASE_PATH);
            obj.put("serial_number", serialNumber.text());
            obj.put("article", "0");
            obj.put("client_article", "0");
            obj.put("status", status.text());
            obj.put("print", engineCycles.text());

            arr_obj = new JSONObject();
            arr = new JSONArray(new ArrayList<String>());
            arr_obj.put("Fuser", maintenanceKitCount.text());
            arr.put(arr_obj);

//            arr_obj = new JSONObject();
//            arr_obj.put("Transfer belt", 123);
//            arr.put(arr_obj);
            obj.put("KIT", (Object) arr);

            arr_obj = new JSONObject();
            arr = new JSONArray(new ArrayList<String>());
            arr_obj.put("black", cartridge.text());
            arr.put(arr_obj);

//            arr_obj = new JSONObject();
//            arr_obj.put("cyan", 60);
//            arr.put(arr_obj);
//
//            arr_obj = new JSONObject();
//            arr_obj.put("magente", 35);
//            arr.put(arr_obj);
//
//            arr_obj = new JSONObject();
//            arr_obj.put("yellow", 100);
//            arr.put(arr_obj);
            obj.put("cartridge", (Object) arr);

//            arr_obj = new JSONObject();
//            arr = new JSONArray(new ArrayList<String>());
//            arr_obj.put("date", "24.09.2019");
//            arr_obj.put("info", "low cartridge");
//            arr.put(arr_obj);
//
//            arr_obj = new JSONObject();
//            arr_obj.put("date", "25.09.2019");
//            arr_obj.put("error", "low cartridge");
//            arr.put(arr_obj);
//            obj.put("log", (Object) arr);

            jsonMessage = obj.toString();
            System.out.println(jsonMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(jsonMessage);
        return jsonMessage;
    }

}
