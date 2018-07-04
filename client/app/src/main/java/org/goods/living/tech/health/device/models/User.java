package org.goods.living.tech.health.device.models;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.goods.living.tech.health.device.BuildConfig;
import org.goods.living.tech.health.device.utils.Constants;
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
    public long updateInterval = Constants.UPDATE_INTERVAL; // seconds
    public String phone;
    public String country;
    public String branch;

    @Transient
    public JSONObject deviceInfoObj;
    public String deviceInfo;

    public int serverApi;
    public boolean forceUpdate = false;
    public boolean disableSync = false;

    public Date lastSync;

    public Date createdAt;
    public Date updatedAt;

    public Date recordedAt;

    public String token;


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
        if (JSONObject.has("branch")) name = JSONObject.getString("branch");
        if (JSONObject.has("country")) name = JSONObject.getString("country");


        if (JSONObject.has("updateInterval"))
            updateInterval = JSONObject.getLong("updateInterval"); // seconds in millis 1000=1

        //   JSONObject.put("versionCode", versionCode);
        if (JSONObject.has("serverApi")) serverApi = JSONObject.getInt("serverApi");
        if (JSONObject.has("forceUpdate")) forceUpdate = JSONObject.getBoolean("forceUpdate");

        if (JSONObject.has("token")) token = JSONObject.getString("token");
    }

    public JSONObject toJSONObject() {

        try {
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
            if (branch != null) JSONObject.put("branch", branch);
            if (country != null) JSONObject.put("country", country);

            if (phone != null) JSONObject.put("phone", phone);

            if (deviceInfo != null) JSONObject.put("deviceInfo", deviceInfo);
            if (deviceInfoObj != null) JSONObject.put("deviceInfo", deviceInfoObj.toString());

            if (token != null) JSONObject.put("token", token);

            int versionCode = BuildConfig.VERSION_CODE;
            String versionName = BuildConfig.VERSION_NAME;
            JSONObject.put("versionCode", versionCode);
            if (versionName != null) JSONObject.put("versionName", versionName);
            JSONObject.put("serverApi", serverApi);

            return JSONObject;
        } catch (JSONException e) {
            Log.e("", "", e);
            Crashlytics.logException(e);
            return null;
        }
    }


}