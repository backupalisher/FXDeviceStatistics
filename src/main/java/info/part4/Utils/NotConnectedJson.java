package info.part4.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class NotConnectedJson {
    public JSONObject errorJson(int device_id, String error) throws JSONException {
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("device_error", "404");
        jsonMessage.put("device_id", device_id);
        jsonMessage.put("error", error);
        System.out.println(jsonMessage.toString());
        return jsonMessage;
    }
}
