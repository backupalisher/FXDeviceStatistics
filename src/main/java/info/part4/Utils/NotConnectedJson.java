package info.part4.Utils;

import info.part4.Controller;
import org.json.JSONException;
import org.json.JSONObject;

public class NotConnectedJson {
    public String errorJson(int user_id, int device_id, String url) throws JSONException {
        String jsonMessage;
        JSONObject obj = new JSONObject();
        //{"init_client_error": 1, "serialNumber":"VCG7428977", "productName":"Kyocera ECOSYS M2540dn",
        // "error": "Нет связи с устройством, по адресу: https://192.168.1.233"}
        obj.put("init_client_error", user_id);
        obj.put("device_id", device_id);
        obj.put("error", "Нет связи с устройством, по адресу: " + url);
        jsonMessage = obj.toString();
        System.out.println(jsonMessage);
        return jsonMessage;
    }
}
