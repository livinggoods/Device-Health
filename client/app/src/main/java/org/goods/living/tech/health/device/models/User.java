package org.goods.living.tech.health.device.models;

import org.goods.living.tech.health.device.BuildConfig;
import org.goods.living.tech.health.device.utils.PermissionsUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class User extends BaseModel {

    @Id
    public long id;
    public Long masterId;
    public String chvId;//uuid
    public String username;
    public String name;
    public String password;
    public String androidId;
    public long updateInterval = PermissionsUtils.UPDATE_INTERVAL; // seconds
    public String phone;
    public String balanceCode;
    public String deviceInfo;

    public int serverApi;
    public boolean forceUpdate = false;
    public boolean disableSync = false;

    public Date lastSync;
    public Date createdAt;
    public Date updatedAt;

    public Date recordedAt;


    public JSONObject toJSONObject() throws JSONException {

        JSONObject JSONObject = super.toJSONObject();
        // JSONObject JSONObject = new JSONObject();
        JSONObject.put("id", String.valueOf(id));
        if (masterId != null) JSONObject.put("masterId", masterId);
        if (chvId != null) JSONObject.put("chvId", chvId);
        if (androidId != null) JSONObject.put("androidId", androidId);
        JSONObject.put("updateInterval", String.valueOf(updateInterval));
        if (createdAt != null) {
            String formattedDate = dateFormat.format(createdAt);
            JSONObject.put("createdAt", formattedDate);
        }
        if (recordedAt != null) {
            String formattedDate = dateFormat.format(recordedAt);
            JSONObject.put("recordedAt", formattedDate);
        }


        if (username != null) JSONObject.put("username", username);
        if (password != null) JSONObject.put("password", password);
        if (name != null) JSONObject.put("name", name);

        if (phone != null) JSONObject.put("phone", phone);

        if (balanceCode != null) JSONObject.put("balanceCode", phone);
        if (deviceInfo != null) JSONObject.put("deviceInfo", deviceInfo);


        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        JSONObject.put("versionCode", versionCode);
        if (versionName != null) JSONObject.put("versionName", versionName);
        JSONObject.put("serverApi", serverApi);

        return JSONObject;
    }

    public static User fromJson(JSONObject JSONObject) throws JSONException {
        User user = new User();
        if (JSONObject.has("id")) user.id = JSONObject.getLong("id");
        if (JSONObject.has("masterId")) user.masterId = JSONObject.getLong("masterId");
        if (JSONObject.has("chvId")) user.chvId = JSONObject.getString("chvId");
        if (JSONObject.has("androidId")) user.androidId = JSONObject.getString("androidId");
        if (JSONObject.has("phone")) user.androidId = JSONObject.getString("phone");
        if (JSONObject.has("balanceCode")) user.androidId = JSONObject.getString("balanceCode");

        if (JSONObject.has("name")) user.name = JSONObject.getString("name");


        if (JSONObject.has("updateInterval"))
            user.updateInterval = JSONObject.getLong("updateInterval"); // seconds in millis 1000=1

        //   JSONObject.put("versionCode", versionCode);
        if (JSONObject.has("disableSync")) user.disableSync = JSONObject.getBoolean("disableSync");
        if (JSONObject.has("serverApi")) user.serverApi = JSONObject.getInt("serverApi");
        if (JSONObject.has("forceUpdate")) user.forceUpdate = JSONObject.getBoolean("forceUpdate");

        return user;

    }
}