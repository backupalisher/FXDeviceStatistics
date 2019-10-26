package info.part4.ParserModels;

import info.part4.Controller;
import info.part4.Utils.GetPageHttps;
import info.part4.Utils.NotConnectedJson;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class HPLaserJet500MFPM525 {
    //HP LaserJet 600 M603
    private static String parserM603(String url) throws KeyManagementException, NoSuchAlgorithmException {
        String jsonMessage = null;
        Document page;

//
        GetPageHttps getPageHttps = new GetPageHttps();
        page = getPageHttps.getPage(url);

        if (page != null) {
            Element status = page.select("span[id=MachineStatus]").first();
            Element cartridge = page.select("span[id=SupplyGauge0]").first();
            Element KIT = page.select("span[id=SupplyGauge1]").first();

            Document configurationPage = getPageHttps.getPage(url + "/hp/device/InternalPages/Index?id=ConfigurationPage");
            Element productName = configurationPage.select("strong[id=ProductName]").first();
            Element serialNumber = configurationPage.select("strong[id=SerialNumber]").first();
            Element maintenanceKitCount = configurationPage.select("strong[id=EngineMaintenanceKitCount]").first();
            Element engineCycles = configurationPage.select("strong[id=EngineCycles]").first();

            JSONObject obj = new JSONObject();
            JSONArray arr;
            JSONObject arr_obj;
            try {
                obj.put("init_client", Controller.USER_ID);
                obj.put("company_id", Controller.COMPANY_ID);
                obj.put("address_id", Controller.ADDRESS_ID);
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
            NotConnectedJson notConnectedJson = new NotConnectedJson();
//            return notConnectedJson.errorJson(url);
        }
        return jsonMessage;
    }
}
