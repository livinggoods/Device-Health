package org.goods.living.tech.health.device.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.loopj.android.http.TextHttpResponseHandler;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.BuildConfig;
import org.goods.living.tech.health.device.R;
import org.goods.living.tech.health.device.UI.UpgradeActivity;
import org.goods.living.tech.health.device.models.DataBalance;
import org.goods.living.tech.health.device.models.Setting;
import org.goods.living.tech.health.device.models.Stats;
import org.goods.living.tech.health.device.models.User;
import org.goods.living.tech.health.device.utils.Constants;
import org.goods.living.tech.health.device.utils.ServerRestClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

@Singleton
public class SyncService extends BaseService {


    @Inject
    StatsService statsService;

    @Inject
    UserService userService;

    @Inject
    DataBalanceService dataBalanceService;

    Context c;


    ServerRestClient serverRestClient = new ServerRestClient(AppController.getInstance().getString(R

            .string.server_url));

    AtomicBoolean syncRunning = new AtomicBoolean(false);

    long SYNC_SIZE = 200;

    static long CLEANUP_LIMIT = 100;

    boolean syncSuccessful = false;

    Date deviceSyncTime;

    @Inject

    public SyncService() {
        //super(boxStore);

    }

    public void sync(Context context) {

        try {
            //synchronized {}

            c = context;
            if (syncRunning.get()) {
                Crashlytics.log(Log.DEBUG, TAG, "sync running exiting...");
                return;
            }
            final User u = userService.getRegisteredUser();
            //only sync after username is registered on device, else just collect data
            if (u.masterId == null) {
                Crashlytics.log("cancel sync. masterid not registered on device yet");
                Answers.getInstance().logCustom(new CustomEvent("Sync failed")
                        .putCustomAttribute("Reason", "masterId missing"));
                return;
            }

            Crashlytics.log("Starting sync");
            syncRunning.set(true);
            deviceSyncTime = new Date();

            serverRestClient.setAuthHeader(u.token);

            User user = syncUser();


            //check server version

            if (user.forceUpdate || BuildConfig.VERSION_CODE < user.serverApi) {
                //  show update dialog ?

                Intent intent = new Intent(context, UpgradeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra(UpgradeActivity.FORCE_UPDATE, user.forceUpdate);
                context.startActivity(intent);


            }
            if (user.forceUpdate) {
                Crashlytics.log("failed sync.forcing update");

                syncRunning.set(false);
                return;
            }

            //only sync if user synced
            if (user.masterId == null) {
                Crashlytics.log("cancel sync stats. masterid does not exist");
                Answers.getInstance().logCustom(new CustomEvent("Sync failed")
                        .putCustomAttribute("Reason", "masterid missing"));
                syncRunning.set(false);
                return;
            }


            syncStats();

            syncDataBalance();


            syncRunning.set(false);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            syncRunning.set(false);
            Crashlytics.logException(e);
        }

    }

    User syncUser() {

        final User user = userService.getRegisteredUser();
        user.deviceTime = deviceSyncTime;

        //  String  deviceSyncTimeStr = Utils.getStringTimeStampWithTimezoneFromDate(deviceSyncTime, TimeZone.getTimeZone(Utils.TIMEZONE_UTC));

        AppController appController = (AppController) c.getApplicationContext();
        Setting setting = appController.getSetting();

        JSONObject userJson = user.toJSONObject(setting);

        StringEntity entity = new StringEntity(userJson.toString(), "UTF-8");
        //RequestParams params = new RequestParams(user.toJSONObject());
        //params.setUseJsonStreamer(true);

        serverRestClient.postSync(Constants.URL.USER_UPDATE, entity, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Crashlytics.log(Log.DEBUG, TAG, "onFailure " + responseString);

                user.lastSync = new Date();
                userService.insertUser(user);

                Crashlytics.logException(throwable);

                refreshToken();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Crashlytics.log(Log.DEBUG, TAG, "onSuccess " + responseString);
                try {
                    JSONObject response = new JSONObject(responseString);
                    String data = response.toString();
                    Crashlytics.log(Log.DEBUG, TAG, "Data : " + data);

                    // If the response is JSONObject instead of JSONArray
                    boolean success = response.has(Constants.STATUS) && response.getBoolean(Constants.STATUS);
                    String msg = response.has(Constants.MESSAGE) ? response.getString(Constants.MESSAGE) : response.getString(Constants.MESSAGE);

                    if (success && response.has(Constants.DATA)) {
                        User updatedUser = new User(response.getJSONObject(Constants.DATA));

                        //         Answers.getInstance().logCustom(new CustomEvent("User Update")
                        //                .putCustomAttribute("Reason", "androidId: " + updatedUser.androidId + " username: " + updatedUser.username));

                        user.masterId = updatedUser.masterId;


                        user.serverApi = updatedUser.serverApi;
                        user.forceUpdate = updatedUser.forceUpdate;
                        user.phone = updatedUser.phone;
                        user.country = updatedUser.country;
                        user.branch = updatedUser.branch;
                        user.name = updatedUser.name;
                        user.chvId = updatedUser.chvId;
                        user.fcmToken = updatedUser.fcmToken;

                        user.lastSync = new Date();
                        userService.insertUser(user);

                    } else {
                        Crashlytics.log(Log.ERROR, TAG, "problem syncing user");
                    }


                } catch (JSONException e) {
                    Log.e(TAG, "", e);
                    Crashlytics.logException(e);
                }
            }
        });

        return user;
    }

    public void refreshToken() {

        final User user = userService.getRegisteredUser();
        user.deviceTime = new Date();

        AppController appController = (AppController) c.getApplicationContext();
        Setting setting = appController.getSetting();

        StringEntity entity = new StringEntity(user.toJSONObject(setting).toString(), "UTF-8");
        //RequestParams params = new RequestParams(user.toJSONObject());
        //params.setUseJsonStreamer(true);

        serverRestClient.postSync(Constants.URL.USERS_REFRESH_TOKEN, entity, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Crashlytics.log(Log.DEBUG, TAG, "onFailure " + responseString);

                user.lastSync = new Date();
                userService.insertUser(user);

                Crashlytics.logException(throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Crashlytics.log(Log.DEBUG, TAG, "onSuccess " + responseString);
                try {
                    JSONObject response = new JSONObject(responseString);
                    String data = response.toString();
                    Crashlytics.log(Log.DEBUG, TAG, "Data : " + data);

                    // If the response is JSONObject instead of JSONArray
                    boolean success = response.has(Constants.STATUS) && response.getBoolean(Constants.STATUS);
                    String msg = response.has(Constants.MESSAGE) ? response.getString(Constants.MESSAGE) : response.getString(Constants.MESSAGE);
                    if (success && response.has(Constants.DATA)) {
                        User updatedUser = new User(response.getJSONObject(Constants.DATA));
                        String oldToken = user.token;
                        user.token = updatedUser.token;

                        if (user.token == null) {//cant auth user.. reset this account
                            user.masterId = null;

                            Crashlytics.log("Refresh token fail");
                            Answers.getInstance().logCustom(new CustomEvent("RefreshToken failed")
                                    .putCustomAttribute("Reason", oldToken));

                        }

                        user.lastSync = new Date();
                        userService.insertUser(user);
                    } else {
                        Crashlytics.log(Log.ERROR, TAG, "problem refreshing user token");
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "", e);
                    Crashlytics.logException(e);
                }
            }
        });
        // return user;
    }

    void syncStats() {

        final User user = userService.getRegisteredUser();

        final List<Stats> list = statsService.getUnSyncedRecords(SYNC_SIZE);

        if (list == null || list.size() == 0) return;

        syncSuccessful = false;

        // JSONObject JSONObject = new JSONObject();
        JSONArray JSONArray = new JSONArray();

        for (Stats s : list) {
            s.deviceTime = deviceSyncTime;
            JSONArray.put(s.toJSONObject());
        }

        StringEntity entity = new StringEntity(JSONArray.toString(), "UTF-8");
        //RequestParams params = new RequestParams(user.toJSONObject());
        //params.setUseJsonStreamer(true);


        serverRestClient.postSync(Constants.URL.STATS_CREATE, entity, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Crashlytics.log(Log.DEBUG, TAG, responseString);
                Crashlytics.log("problem syncing stats: " + responseString);
                Crashlytics.logException(throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Crashlytics.log(Log.DEBUG, TAG, "data " + responseString);
                try {
                    JSONObject response = new JSONObject(responseString);
                    String data = response.toString();
                    Crashlytics.log(Log.DEBUG, TAG, data);

                    // If the response is JSONObject instead of JSONArray
                    boolean success = response.has(Constants.STATUS) && response.getBoolean(Constants.STATUS);
                    String msg = response.has(Constants.MESSAGE) ? response.getString(Constants.MESSAGE) : response.getString(Constants.MESSAGE);

                    if (success) {
                        Crashlytics.log("Successfully synced stats");
                        syncSuccessful = true;
                        for (Stats s : list) {
                            s.synced = true;
                        }
                        statsService.insertRecords(list);
                        //      Answers.getInstance().logCustom(new CustomEvent("Sync stats")
                        //              .putCustomAttribute("Reason", "androidId: " + user.androidId + " username: " + user.username));


                        //cleanup
                        statsService.deleteSyncedRecords(list);//deleteSyncedRecordsOlder(CLEANUP_LIMIT);

                    } else {
                        Crashlytics.log(Log.DEBUG, TAG, "problem syncing stats");
                        Crashlytics.log("problem syncing stats");
                        Crashlytics.logException(new Exception("problem syncing stats "));
                    }


                } catch (JSONException e) {
                    //  e.printStackTrace();
                    Log.e(TAG, "", e);
                    Crashlytics.logException(e);
                }
            }
        });


        List<Stats> listMore = statsService.getUnSyncedRecords(SYNC_SIZE);
        if (listMore.size() > 0 && syncSuccessful) {
            syncStats();
        }

    }

    void syncDataBalance() {


        final List<DataBalance> list = dataBalanceService.getUnSyncedRecords(SYNC_SIZE);


        if (list == null || list.size() == 0) return;
        syncSuccessful = false;

        // JSONObject JSONObject = new JSONObject();
        JSONArray JSONArray = new JSONArray();

        for (DataBalance d : list) {

            JSONArray.put(d.toJSONObject());
        }

        StringEntity entity = new StringEntity(JSONArray.toString(), "UTF-8");
        //RequestParams params = new RequestParams(user.toJSONObject());
        //params.setUseJsonStreamer(true);


        serverRestClient.postSync(Constants.URL.DATABALANCE_CREATE, entity, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Crashlytics.log(Log.DEBUG, TAG, responseString);
                Crashlytics.log("problem syncing databalance: " + responseString);
                Crashlytics.logException(throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Crashlytics.log(Log.DEBUG, TAG, "data " + responseString);
                try {
                    JSONObject response = new JSONObject(responseString);
                    String data = response.toString();
                    Crashlytics.log(Log.DEBUG, TAG, "Data : " + data);

                    // If the response is JSONObject instead of JSONArray
                    boolean success = response.has(Constants.STATUS) && response.getBoolean(Constants.STATUS);
                    String msg = response.has(Constants.MESSAGE) ? response.getString(Constants.MESSAGE) : response.getString(Constants.MESSAGE);

                    if (success) {
                        Crashlytics.log("Successfully synced databalance");
                        syncSuccessful = true;

                        for (DataBalance d : list) {
                            d.synced = true;
                        }
                        dataBalanceService.insertRecords(list);
                        //      Answers.getInstance().logCustom(new CustomEvent("Sync stats")
                        //              .putCustomAttribute("Reason", "androidId: " + user.androidId + " username: " + user.username));


                        //cleanup
                        dataBalanceService.deleteSyncedRecords(list);//deleteSyncedRecordsOlder(CLEANUP_LIMIT);

                    } else {
                        Crashlytics.log(Log.DEBUG, TAG, "problem syncing databalance");
                        Crashlytics.log("problem syncing databalance");
                        Crashlytics.logException(new Exception("problem syncing databalance "));
                    }


                } catch (JSONException e) {
                    //  e.printStackTrace();
                    Log.e(TAG, "", e);
                    Crashlytics.logException(e);
                }
            }
        });


        List<DataBalance> listMore = dataBalanceService.getUnSyncedRecords(SYNC_SIZE);
        if (listMore.size() > 0 && syncSuccessful) {
            syncDataBalance();
        }

    }

    Setting syncSetting() {

        AppController appController = (AppController) c.getApplicationContext();
        Setting setting = appController.getSetting();
        StringEntity entity = new StringEntity(setting.toJSONObject().toString(), "UTF-8");
        //RequestParams params = new RequestParams(user.toJSONObject());
        //params.setUseJsonStreamer(true);

        serverRestClient.postSync(Constants.URL.USER_SETTING, entity, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Crashlytics.log(Log.DEBUG, TAG, "onFailure " + responseString);
                Crashlytics.logException(throwable);
                refreshToken();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Crashlytics.log(Log.DEBUG, TAG, "onSuccess " + responseString);
                try {
                    JSONObject response = new JSONObject(responseString);
                    String data = response.toString();
                    Crashlytics.log(Log.DEBUG, TAG, "Data : " + data);

                    // If the response is JSONObject instead of JSONArray
                    boolean success = response.has(Constants.STATUS) && response.getBoolean(Constants.STATUS);
                    String msg = response.has(Constants.MESSAGE) ? response.getString(Constants.MESSAGE) : response.getString(Constants.MESSAGE);
                    boolean changeLocationUpdateInterval = false;
                    if (success && response.has(Constants.DATA)) {
                        Setting updatedSetting = new Setting(response.getJSONObject(Constants.DATA));


                        changeLocationUpdateInterval = setting.locationUpdateInterval != updatedSetting.locationUpdateInterval;
                        setting.locationUpdateInterval = updatedSetting.locationUpdateInterval;

                        if (updatedSetting.ussd != null && setting.workingUSSD0 == null) {
                            ArrayList<String> list = USSDService.getUSSDCodesFromString(updatedSetting.ussd);
                            setting.workingUSSD0 = list;
                        }
                        if (updatedSetting.ussd != null && setting.workingUSSD1 == null) {
                            ArrayList<String> list = USSDService.getUSSDCodesFromString(updatedSetting.ussd);
                            setting.workingUSSD1 = list;
                        }
                        setting.databalanceCheckTime = updatedSetting.databalanceCheckTime;

                        AppController.getInstance().updateSetting(setting);

                        Answers.getInstance().logCustom(new CustomEvent("Setting Update").putCustomAttribute("Reason", setting.toJSONObject().toString()));

                        appController.setUSSDAlarm(setting.getDatabalanceCheckTimeInMilli());

                        if (changeLocationUpdateInterval) {
                            AppController appController = (AppController) c.getApplicationContext();
                            appController.requestLocationUpdates(setting.locationUpdateInterval);
                        }

                    } else {
                        Crashlytics.log(Log.ERROR, TAG, "problem syncing settings");
                    }


                } catch (JSONException e) {
                    Log.e(TAG, "", e);
                    Crashlytics.logException(e);
                }
            }
        });


        return setting;
    }
}
