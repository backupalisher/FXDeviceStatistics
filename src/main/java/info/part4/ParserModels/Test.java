package info.part4.ParserModels;

import info.part4.Controller;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Test {
    public String parser(String url) throws JSONException {
        JSONObject obj = new JSONObject();
        JSONArray arr;
        JSONObject arr_obj;

        obj.put("client_init", "putDevices");
        obj.put("company_id", Controller.COMPANY_ID);
        obj.put("url", url);
        obj.put("article", "0");
        obj.put("printCycles", "525");
        obj.put("device_id", Controller.DEVICE_ID);
        obj.put("status", "Готов");
        obj.put("productName", "HP LaserJet M1212 MFP");
        obj.put("serialNumber", "NCB55F9657S1");

        arr_obj = new JSONObject();
        arr = new JSONArray(new ArrayList<String>());
        arr_obj.put("black", "75");
        arr.put(arr_obj);
        obj.put("cartridge", arr);

        System.out.println(obj.toString());
        return obj.toString();
    }
}
