package info.part4.ParserModels;

import info.part4.Utils.GetPageHttps;
import info.part4.Utils.NotConnectedJson;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class BrotherL2700DN {
    //Brother MFC-L2700DN
    public String parser(String url) throws KeyManagementException, NoSuchAlgorithmException, IOException {
        String jsonMessage = null;
        Document page;

        GetPageHttps getPageHttps = new GetPageHttps();
        page = getPageHttps.getPage(url);

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
            NotConnectedJson notConnectedJson = new NotConnectedJson();
//            return notConnectedJson.errorJson(url);
        }
        System.out.println(jsonMessage);
        return jsonMessage;
    }
}
