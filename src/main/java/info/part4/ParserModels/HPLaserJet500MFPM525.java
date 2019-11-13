package info.part4.ParserModels;

import info.part4.Controller;
import info.part4.Utils.GetPageHttps;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class HPLaserJet500MFPM525 {
    //HP LaserJet 500 MFP M525
    private static String parser(String url) throws KeyManagementException, NoSuchAlgorithmException {
        String jsonMessage = null;
        Document page;

        GetPageHttps getPageHttps = new GetPageHttps();
        page = getPageHttps.getPage(url);

        if (page != null) {
            Element status = page.select("span[id=MachineStatus]").first();
            Element cartridge = page.select("span[id=SupplyGauge0]").first();
            Document configurationPage = getPageHttps.getPage(url + "/hp/device/InternalPages/Index?id=ConfigurationPage");
            Element productName = configurationPage.select("strong[id=ProductName]").first();
            Element serialNumber = configurationPage.select("strong[id=SerialNumber]").first();
            Element adfCycles = configurationPage.select("strong[id=ADFMaintenance]").first();
            Element engineCycles = configurationPage.select("strong[id=EngineCycles]").first();

            JSONObject obj = new JSONObject();
            JSONArray arr;
            JSONObject arr_obj;
            try {
                obj.put("init_client", Controller.OFFICE_ID);
                obj.put("company_id", Controller.COMPANY_ID);
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
                obj.put("KIT", arr);
                arr_obj = new JSONObject();
                arr = new JSONArray(new ArrayList<String>());
                arr_obj.put("black", cartridge.text());
                arr.put(arr_obj);
                obj.put("cartridge", arr);
                jsonMessage = obj.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return jsonMessage;
    }
}
