package org.goods.living.tech.health.device.models;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.goods.living.tech.health.device.BuildConfig;
import org.goods.living.tech.health.device.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.TimeZone;

import io.objectbox.annotation.Convert;
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
    public String phone;
    public String country;
    public String branch;

    @Convert(converter = JSonObjectConverter.class, dbType = String.class)
    public JSONObject deviceInfo;


    public int serverApi;
    public boolean forceUpdate = false;


    public boolean disableSync = false;

    public Date lastSync;

    public Date createdAt;
    public Date updatedAt;

    public Date recordedAt;

    public String token;

    public String fcmToken;


    public User() {
        super();
    }

    public User(JSONObject JSONObject) throws JSONException {

        super(JSONObject);

        if (JSONObject.has("id")) id = JSONObject.getLong("id");
        if (JSONObject.has("masterId")) masterId = JSONObject.getLong("masterId");
        if (JSONObject.has("chvId")) chvId = JSONObject.getString("chvId");
        if (JSONObject.has("androidId")) androidId = JSONObject.getString("androidId");
        if (JSONObject.has("phone")) phone = JSONObject.getString("phone");

        if (JSONObject.has("name")) name = JSONObject.getString("name");
        if (JSONObject.has("branch")) branch = JSONObject.getString("branch");
        if (JSONObject.has("country")) country = JSONObject.getString("country");


        //   JSONObject.put("versionCode", versionCode);
        if (JSONObject.has("serverApi")) serverApi = JSONObject.getInt("serverApi");
        if (JSONObject.has("forceUpdate")) forceUpdate = JSONObject.getBoolean("forceUpdate");

        if (JSONObject.has("token")) token = JSONObject.getString("token");

        if (JSONObject.has("fcmToken")) fcmToken = JSONObject.getString("fcmToken");

    }

    public JSONObject toJSONObject(Setting setting) {

        try {
            JSONObject JSONObject = super.toJSONObject();
            // JSONObject JSONObject = new JSONObject();
            JSONObject.put("id", String.valueOf(id));
            if (masterId != null) JSONObject.put("masterId", masterId);
            if (chvId != null) JSONObject.put("chvId", chvId);
            if (androidId != null) JSONObject.put("androidId", androidId);
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
            if (branch != null) JSONObject.put("branch", branch);
            if (country != null) JSONObject.put("country", country);

            if (phone != null) JSONObject.put("phone", phone);

            if (deviceInfo != null) JSONObject.put("deviceInfo", deviceInfo);

            if (token != null) JSONObject.put("token", token);

            if (fcmToken != null) JSONObject.put("fcmToken", fcmToken);

            int versionCode = BuildConfig.VERSION_CODE;
            String versionName = BuildConfig.VERSION_NAME;
            JSONObject.put("versionCode", versionCode);
            if (versionName != null) JSONObject.put("versionName", versionName);
            JSONObject.put("serverApi", serverApi);

            if (deviceTime != null) {
                String formattedDate = Utils.getStringTimeStampWithTimezoneFromDate(deviceTime, TimeZone.getTimeZone(Utils.TIMEZONE_UTC));
                JSONObject.put("deviceTime", formattedDate);
            }

            if (setting != null) {
                JSONObject.put("setting", setting.toJSONObject());
            }

            return JSONObject;
        } catch (JSONException e) {
            Log.e("", "", e);
            Crashlytics.logException(e);
            return null;
        }
    }


}