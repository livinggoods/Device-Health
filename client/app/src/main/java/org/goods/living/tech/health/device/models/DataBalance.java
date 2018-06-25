package org.goods.living.tech.health.device.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class DataBalance extends BaseModel {

    @Id
    public long id;
    public long userId; // ToOne target ID property
    public Long userMasterId;
    //  public ToOne<User> user;

    public double balance;
    public String text;
    public String message;
    public Date recordedAt;

    public boolean synced;
    public Date createdAt;

    public JSONObject toJSONObject() throws JSONException {

        JSONObject JSONObject = super.toJSONObject();
        JSONObject.put("id", String.valueOf(id));
        JSONObject.put("userId", userId);
        if (userMasterId != null) JSONObject.put("userMasterId", userMasterId);
        if (text != null) JSONObject.put("text", text);
        JSONObject.put("balance", balance);
        if (message != null) JSONObject.put("message", message);
        if (recordedAt != null) {
            String formattedDate = dateFormat.format(recordedAt);
            JSONObject.put("recordedAt", formattedDate);
        }


        return JSONObject;
    }
}
