package info.part4.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class NotConnectedJson {
    public JSONObject errorJson(int company_id, int device_id) throws JSONException {
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("client_init_error", company_id);
        jsonMessage.put("device_id", device_id);
        jsonMessage.put("error", "Нет связи с устройством");
        System.out.println(jsonMessage.toString());
        return jsonMessage;
    }
}
