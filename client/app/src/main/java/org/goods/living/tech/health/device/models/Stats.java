package org.goods.living.tech.health.device.models;

import org.goods.living.tech.health.device.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.TimeZone;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Stats extends BaseModel {

    @Id
    public long id;

    public double latitude;
    public double longitude;
    public double accuracy;
    public String provider;
    public String message;
    public Date recordedAt;

    public boolean synced;
    public Date createdAt;
    Date updatedAt;


    public Stats() {

    }


    public JSONObject toJSONObject() throws JSONException {

        JSONObject JSONObject = super.toJSONObject();

        JSONObject.put("id", String.valueOf(id));
        JSONObject.put("latitude", latitude);
        JSONObject.put("longitude", longitude);
        JSONObject.put("accuracy", accuracy);
        if (provider != null) JSONObject.put("provider", provider);
        if (message != null) JSONObject.put("message", message);
        if (recordedAt != null) {
            String formattedDate = Utils.getStringTimeStampWithTimezoneFromDate(recordedAt, TimeZone.getTimeZone(Utils.TIMEZONE_UTC));
            JSONObject.put("recordedAt", formattedDate);
        }


        return JSONObject;
    }
}
