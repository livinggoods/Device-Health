package org.goods.living.tech.health.device.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Transient;

@Entity
public class Stats {

    @Id
    public long id;
    public long userId; // ToOne target ID property
    public Long userMasterId;
    //  public ToOne<User> user;

    public double latitude;
    public double longitude;
    public double accuracy;
    public String provider;
    public Date recordedAt;

    public boolean synced;
    public Date createdAt;
    Date updatedAt;

    @Transient
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
    //String formattedDate = dateFormat.format(date);

    public JSONObject toJSONObject() throws JSONException {
        JSONObject JSONObject = new JSONObject();
        JSONObject.put("id", String.valueOf(id));
        JSONObject.put("userId", userId);
        if (userMasterId != null) JSONObject.put("userMasterId", userMasterId);
        JSONObject.put("latitude", latitude);
        JSONObject.put("longitude", longitude);
        JSONObject.put("accuracy", accuracy);
        if (provider != null) JSONObject.put("provider", provider);
        if (recordedAt != null) {
            String formattedDate = dateFormat.format(recordedAt);
            JSONObject.put("recordedAt", formattedDate);
        }


        return JSONObject;
    }
}
