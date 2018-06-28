package org.goods.living.tech.health.device.models;

import org.goods.living.tech.health.device.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.TimeZone;

public class BaseModel {

    //String formattedDate = dateFormat.format(date);
    public Date deviceTime;//at sync/toJSONObject time set this - we can use it to get the clock drift
    public Long clockDrift;//clock drift from server time in seconds

    public BaseModel() {

    }

    public BaseModel(JSONObject JSONObject) throws JSONException {

        if (JSONObject.has("clockDrift")) clockDrift = JSONObject.getLong("clockDrift");

    }

    public JSONObject toJSONObject() throws JSONException {

        JSONObject JSONObject = new JSONObject();
        // JSONObject.put("addsharedfields", api);
        if (deviceTime != null) {
            String formattedDate = Utils.getStringTimeStampWithTimezoneFromDate(deviceTime, TimeZone.getTimeZone(Utils.TIMEZONE_UTC));
            JSONObject.put("recordedAt", formattedDate);
        }
        if (JSONObject.has("clockDrift")) clockDrift = JSONObject.getLong("clockDrift");
        

        return JSONObject;
    }
}
