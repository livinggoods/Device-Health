package org.goods.living.tech.health.device.models;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.goods.living.tech.health.device.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.TimeZone;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Transient;

@Entity
public class DataBalance extends BaseModel {

    @Id
    public long id;

    public Double balance;
    public String balanceMessage;
    public String message;
    public String phone;

    @Transient
    public JSONObject infoObj;
    public String info;

    public Date recordedAt;

    public boolean synced;
    public Date createdAt;

    public JSONObject toJSONObject() {

        try {
            JSONObject JSONObject = super.toJSONObject();
            JSONObject.put("id", String.valueOf(id));
            if (balance != null) JSONObject.put("balance", balance);
            if (balanceMessage != null) JSONObject.put("balanceMessage", balanceMessage);
            if (message != null) JSONObject.put("message", message);
            if (recordedAt != null) {
                String formattedDate = Utils.getStringTimeStampWithTimezoneFromDate(recordedAt, TimeZone.getTimeZone(Utils.TIMEZONE_UTC));
                JSONObject.put("recordedAt", formattedDate);
            }

            if (info != null) JSONObject.put("info", info.toString());
            if (infoObj != null) JSONObject.put("info", infoObj.toString());

            return JSONObject;
        } catch (JSONException e) {
            Log.e("", "", e);
            Crashlytics.logException(e);
            return null;
        }
    }
}
