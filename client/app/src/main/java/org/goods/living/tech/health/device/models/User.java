package org.goods.living.tech.health.device.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class User {

    @Id
    public long id;
    public Long masterId;
    public String chpId;
    public String phoneNumber;
    public String androidId;
    public long updateInterval = 60000; // seconds in millis 1000=1
    public boolean shouldSync;//if we need to sync to backend any new changes


    public Date lastSync;
    public Date createdAt;
    public Date updatedAt;


    public HashMap<String, String> toHashMap() {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("id", String.valueOf(id));
        if (masterId != null) hashMap.put("masterId", masterId.toString());
        if (chpId != null) hashMap.put("chpId", chpId.toString());
        if (phoneNumber != null) hashMap.put("phoneNumber", phoneNumber.toString());
        if (androidId != null) hashMap.put("androidId", androidId);
        hashMap.put("updateInterval", String.valueOf(updateInterval));

        return hashMap;
    }

    public static User fromJson(JSONObject JSONObject) throws JSONException {
        User user = new User();
        if (JSONObject.has("id")) user.id = JSONObject.getLong("id");
        if (JSONObject.has("masterId")) user.masterId = JSONObject.getLong("id");
        if (JSONObject.has("chpId")) user.chpId = JSONObject.getString("id");
        if (JSONObject.has("phoneNumber")) user.phoneNumber = JSONObject.getString("id");
        if (JSONObject.has("androidId")) user.androidId = JSONObject.getString("id");
        if (JSONObject.has("updateInterval"))
            user.updateInterval = JSONObject.getLong("id"); // seconds in millis 1000=1

        return user;

    }
}