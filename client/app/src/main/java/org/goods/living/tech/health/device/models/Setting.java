package org.goods.living.tech.health.device.models;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.goods.living.tech.health.device.utils.Constants;
import org.goods.living.tech.health.device.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.converter.PropertyConverter;

@Entity
public class Setting extends BaseModel {

    @Id
    public long id;

    public boolean loglocationOffEvent;

    public Double brightness;

    public Date lastUSSDRun;

    public long locationUpdateInterval = Constants.UPDATE_INTERVAL; // seconds

    public boolean forceUpdate = false;
    public boolean disableSync = false;
    public int serverApi;


    public String databalanceCheckTime = "7:00";//"20:30
    public boolean disableDatabalanceCheck = false;

    public String ussd;

    public String network;

    public String simSelection;

    // "
    //  @Convert(converter = StringListConverter.class, dbType = String.class)
    // public List<String> workingUSSD;

    public Setting() {

    }

    public Setting(JSONObject JSONObject) throws JSONException {

        super(JSONObject);

        if (JSONObject.has("id")) id = JSONObject.getLong("id");
        //  if (JSONObject.has("masterId")) masterId = JSONObject.getLong("masterId");


        if (JSONObject.has("locationUpdateInterval"))
            locationUpdateInterval = JSONObject.getLong("locationUpdateInterval"); // seconds in millis 1000=1

        if (JSONObject.has("databalanceCheckTime"))
            databalanceCheckTime = JSONObject.getString("databalanceCheckTime");

        if (JSONObject.has("forceUpdate"))
            forceUpdate = JSONObject.getBoolean("forceUpdate");
        if (JSONObject.has("disableSync"))
            disableSync = JSONObject.getBoolean("disableSync");
        if (JSONObject.has("serverApi"))
            serverApi = JSONObject.getInt("serverApi");
        if (JSONObject.has("disableDatabalanceCheck"))
            disableDatabalanceCheck = JSONObject.getBoolean("disableDatabalanceCheck");

    }

    public JSONObject toJSONObject() {

        try {
            JSONObject JSONObject = super.toJSONObject();
            // JSONObject JSONObject = new JSONObject();
            JSONObject.put("id", String.valueOf(id));
            //   if (masterId != null) JSONObject.put("masterId", masterId);
            JSONObject.put("loglocationOffEvent", loglocationOffEvent);
            if (brightness != null) JSONObject.put("brightness", brightness);
            if (lastUSSDRun != null) {
                String formattedDate = Utils.getStringTimeStampWithTimezoneFromDate(lastUSSDRun, TimeZone.getTimeZone(Utils.TIMEZONE_UTC));
                JSONObject.put("lastUSSDRun", lastUSSDRun);
            }
            JSONObject.put("locationUpdateInterval", locationUpdateInterval);
            if (databalanceCheckTime != null)
                JSONObject.put("databalanceCheckTime", databalanceCheckTime);
            if (ussd != null) JSONObject.put("ussd", ussd);

            JSONObject.put("simSelection", simSelection);
            JSONObject.put("network", network);

            JSONObject.put("forceUpdate", forceUpdate);
            JSONObject.put("disableSync", disableSync);
            JSONObject.put("serverApi", serverApi);
            JSONObject.put("disableDatabalanceCheck", disableDatabalanceCheck);


            return JSONObject;
        } catch (JSONException e) {
            Log.e("", "", e);
            Crashlytics.logException(e);
            return null;
        }
    }


    public static class StringListConverter implements PropertyConverter<List<String>, String> {

        @Override
        public List<String> convertToEntityProperty(String jsonString) {
            if (jsonString == null) {
                return null;
            }


            try {
                ArrayList<String> list = new ArrayList<String>();
                JSONArray response = new JSONArray(jsonString);
                if (response != null) {
                    int len = response.length();
                    for (int i = 0; i < len; i++) {
                        list.add(response.get(i).toString());
                    }
                }
                return list;

            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            //  return new Gson().fromJson(jsonString, new TypeToken<ArrayList<String>>() {
            //  }.getType());
        }

        @Override
        public String convertToDatabaseValue(List<String> list) {
            if (list == null) {
                return null;
            }

            JSONArray response = new JSONArray();
            if (response != null) {
                int len = response.length();
                for (int i = 0; i < list.size(); i++) {
                    response.put(list.get(i).toString());
                }
            }

            return response.toString();//new Gson().toJson(creatureList);
        }
    }

    public Long getDatabalanceCheckTimeInMilli() {

        try {

            SimpleDateFormat df = new SimpleDateFormat("H:mm"); //a for 24hr
            Date d1 = df.parse(databalanceCheckTime);//df.parse( "23:30");

            GregorianCalendar timepoint = new GregorianCalendar();
            timepoint.setTime(d1);
            int hours = timepoint.get(Calendar.HOUR_OF_DAY);
            int minutes = timepoint.get(Calendar.MINUTE);

            Calendar calendar = Calendar.getInstance();//(timeZone);

            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hours);
            calendar.set(Calendar.MINUTE, minutes);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);


            Crashlytics.log(Log.DEBUG, this.getClass().getSimpleName(), "DatabalanceCheckTime " + calendar.getTime());
            return calendar.getTimeInMillis();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}