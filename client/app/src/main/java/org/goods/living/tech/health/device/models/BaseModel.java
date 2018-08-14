package org.goods.living.tech.health.device.models;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.goods.living.tech.health.device.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.TimeZone;

import io.objectbox.converter.PropertyConverter;

public class BaseModel {

    //String formattedDate = dateFormat.format(date);
    public Date deviceTime;//at sync/toJSONObject time set this - we can use it to get the clock drift
    public Long clockDrift;//clock drift from server time in seconds

    public BaseModel() {

    }

    public BaseModel(JSONObject JSONObject) throws JSONException {

        if (JSONObject.has("clockDrift")) clockDrift = JSONObject.getLong("clockDrift");

    }

    JSONObject toJSONObject() {

        try {
            JSONObject JSONObject = new JSONObject();
            // JSONObject.put("addsharedfields", api);
            if (deviceTime != null) {
                String formattedDate = Utils.getStringTimeStampWithTimezoneFromDate(deviceTime, TimeZone.getTimeZone(Utils.TIMEZONE_UTC));
                JSONObject.put("recordedAt", formattedDate);
            }
            if (JSONObject.has("clockDrift")) clockDrift = JSONObject.getLong("clockDrift");


            return JSONObject;
        } catch (JSONException e) {
            Log.e("", "", e);
            Crashlytics.logException(e);
            return null;
        }
    }

    public static class JSonObjectConverter implements PropertyConverter<JSONObject, String> {


        @Override
        public JSONObject convertToEntityProperty(String databaseValue) {

            try {
                if (databaseValue == null)
                    return null;

                return new JSONObject(databaseValue);
            } catch (JSONException e) {
                Log.e("", "", e);
                Crashlytics.logException(e);
                return null;
            }
        }

        @Override
        public String convertToDatabaseValue(JSONObject entityProperty) {
            return entityProperty == null ? null : entityProperty.toString();
        }
    }
}
