package org.goods.living.tech.health.device.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

import io.objectbox.annotation.Transient;

public class BaseModel {


    @Transient
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

    //String formattedDate = dateFormat.format(date);


    public JSONObject toJSONObject() throws JSONException {

        JSONObject JSONObject = new JSONObject();
        // JSONObject.put("addsharedfields", api);

        return JSONObject;
    }
}
