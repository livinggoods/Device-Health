package org.goods.living.tech.health.device.models;

import org.goods.living.tech.health.device.BuildConfig;
import org.goods.living.tech.health.device.utils.PermissionsUtils;
import org.goods.living.tech.health.device.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.TimeZone;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Transient;

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

    @Transient
    public JSONObject deviceInfo;

    public int serverApi;
    public boolean forceUpdate = false;
    public boolean disableSync = false;

    public Date lastSync;

    public Date createdAt;
    public Date updatedAt;

    public Date recordedAt;


    public User() {
        super();
    }

    public User(JSONObject JSONObject) throws JSONException {

        super(JSONObject);

        if (JSONObject.has("id")) id = JSONObject.getLong("id");
        if (JSONObject.has("masterId")) masterId = JSONObject.getLong("masterId");
        if (JSONObject.has("chvId")) chvId = JSONObject.getString("chvId");
        if (JSONObject.has("androidId")) androidId = JSONObject.getString("androidId");
        if (JSONObject.has("phone")) androidId = JSONObject.getString("phone");
        if (JSONObject.has("balanceCode")) androidId = JSONObject.getString("balanceCode");

        if (JSONObject.has("name")) name = JSONObject.getString("name");


        if (JSONObject.has("updateInterval"))
            updateInterval = JSONObject.getLong("updateInterval"); // seconds in millis 1000=1

        //   JSONObject.put("versionCode", versionCode);
        if (JSONObject.has("disableSync")) disableSync = JSONObject.getBoolean("disableSync");
        if (JSONObject.has("serverApi")) serverApi = JSONObject.getInt("serverApi");
        if (JSONObject.has("forceUpdate")) forceUpdate = JSONObject.getBoolean("forceUpdate");


    }

    public JSONObject toJSONObject() throws JSONException {

        JSONObject JSONObject = super.toJSONObject();
        // JSONObject JSONObject = new JSONObject();
        JSONObject.put("id", String.valueOf(id));
        if (masterId != null) JSONObject.put("masterId", masterId);
        if (chvId != null) JSONObject.put("chvId", chvId);
        if (androidId != null) JSONObject.put("androidId", androidId);
        JSONObject.put("updateInterval", String.valueOf(updateInterval));
        if (createdAt != null) {
            String formattedDate = Utils.getStringTimeStampWithTimezoneFromDate(createdAt, TimeZone.getTimeZone(Utils.TIMEZONE_UTC));
            JSONObject.put("createdAt", formattedDate);
        }
        if (recordedAt != null) {
            String formattedDate = Utils.getStringTimeStampWithTimezoneFromDate(recordedAt, TimeZone.getTimeZone(Utils.TIMEZONE_UTC));
            JSONObject.put("recordedAt", formattedDate);
        }

        if (username != null) JSONObject.put("username", username);
        if (password != null) JSONObject.put("password", password);
        if (name != null) JSONObject.put("name", name);

        if (phone != null) JSONObject.put("phone", phone);

        if (balanceCode != null) JSONObject.put("balanceCode", phone);
        if (deviceInfo != null) JSONObject.put("deviceInfo", deviceInfo.toString());


        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        JSONObject.put("versionCode", versionCode);
        if (versionName != null) JSONObject.put("versionName", versionName);
        JSONObject.put("serverApi", serverApi);

        return JSONObject;
    }


}