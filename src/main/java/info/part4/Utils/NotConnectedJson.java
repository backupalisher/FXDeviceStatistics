package info.part4.Utils;

import info.part4.Controller;
import org.json.JSONObject;

public class NotConnectedJson {
    public String errorJson(String url) {
        String jsonMessage;
        JSONObject obj = new JSONObject();
        //{"init_client_error": 1, "serialNumber":"VCG7428977", "productName":"Kyocera ECOSYS M2540dn",
        // "error": "Нет связи с устройством, по адресу: https://192.168.1.233"}
        obj.put("init_client_error", Controller.USER_ID);
        obj.put("serialNumber", Controller.SERIAL_NUMBER);
        obj.put("productName", Controller.DEVICE_NAME);
        obj.put("error", "Нет связи с устройством, по адресу: " + url);
        jsonMessage = obj.toString();
        System.out.println(jsonMessage);
        return jsonMessage;
    }
}
