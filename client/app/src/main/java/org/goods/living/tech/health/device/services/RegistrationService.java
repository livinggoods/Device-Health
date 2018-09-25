package org.goods.living.tech.health.device.services;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.loopj.android.http.TextHttpResponseHandler;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.BuildConfig;
import org.goods.living.tech.health.device.R;
import org.goods.living.tech.health.device.UI.UpgradeActivity;
import org.goods.living.tech.health.device.models.Setting;
import org.goods.living.tech.health.device.models.User;
import org.goods.living.tech.health.device.utils.Constants;
import org.goods.living.tech.health.device.utils.DataBalanceHelper;
import org.goods.living.tech.health.device.utils.ServerRestClient;
import org.goods.living.tech.health.device.utils.SnackbarUtil;
import org.goods.living.tech.health.device.utils.TelephonyUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

@Singleton
public class RegistrationService extends BaseService {


    @Inject
    StatsService statsService;

    @Inject
    UserService userService;

    @Inject
    DataBalanceService dataBalanceService;

    @Inject
    DataBalanceHelper dataBalanceHelper;
    @Inject
    SettingService settingService;


    ServerRestClient serverRestClient = new ServerRestClient(AppController.getInstance().getString(R

            .string.server_url));

    @Inject
    public RegistrationService() {
        //super(boxStore);

    }

    public User register(Activity c) {

        final User user = userService.getRegisteredUser();
        user.deviceTime = new Date();
        Setting setting = settingService.getRecord();

        StringEntity entity = new StringEntity(user.toJSONObject(setting).toString(), "UTF-8");
        //RequestParams params = new RequestParams(user.toJSONObject());
        //params.setUseJsonStreamer(true);

        serverRestClient.postSync(Constants.URL.USER_CREATE, entity, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Crashlytics.log(Log.DEBUG, TAG, responseString);
                try {
                    Crashlytics.logException(throwable);
                    if (responseString == null) return;
                    JSONObject response = new JSONObject(responseString);
                    String msg = response.has(Constants.MESSAGE) ? response.getString(Constants.MESSAGE) : response.getString(Constants.MESSAGE);
                    Answers.getInstance().logCustom(new CustomEvent("User Registration fail")
                            .putCustomAttribute("Reason", "username: " + user.username + " msg: " + msg));

                    user.lastSync = new Date();
                    userService.insertUser(user);


                } catch (JSONException e) {
                    Log.e(TAG, "", e);
                    Crashlytics.logException(e);
                }

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Crashlytics.log(Log.DEBUG, TAG, "onSuccess " + responseString);
                try {
                    JSONObject response = new JSONObject(responseString);
                    String data = response.toString();
                    Crashlytics.log(Log.DEBUG, TAG, data);

                    boolean success = response.has(Constants.STATUS) && response.getBoolean(Constants.STATUS);
                    String msg = response.has(Constants.MESSAGE) ? response.getString(Constants.MESSAGE) : response.getString(Constants.MESSAGE);

                    if (success && response.has(Constants.DATA)) {
                        User updatedUser = new User(response.getJSONObject(Constants.DATA));

                        Answers.getInstance().logCustom(new CustomEvent("User Registration")
                                .putCustomAttribute("Reason", "masterId: " + updatedUser.masterId + " user: " + updatedUser.username));

                        user.masterId = updatedUser.masterId;
                        user.serverApi = updatedUser.serverApi;
                        user.phone = updatedUser.phone;
                        user.country = updatedUser.country;
                        user.branch = updatedUser.branch;
                        user.name = updatedUser.name;
                        user.chvId = updatedUser.chvId;
                        user.token = updatedUser.token;

                        user.lastSync = new Date();
                        userService.insertUser(user);

                        serverRestClient.setAuthHeader(user.token);

                    } else {
                        Crashlytics.log(Log.ERROR, TAG, "problem registering user " + user.username + " " + msg);
                        Answers.getInstance().logCustom(new CustomEvent("User Registration fail")
                                .putCustomAttribute("Reason", "username: " + user.username + " msg: " + msg));

                        SnackbarUtil.showSnackLong(c, msg);
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "", e);
                    Crashlytics.logException(e);
                }
            }
        });
        return user;
    }

    public String getUSSDCodes() {

        final User user = userService.getRegisteredUser();
        Setting setting = AppController.getInstance().getSetting();
        Crashlytics.log(Log.DEBUG, TAG, "getUSSDCodes");


        //  String  deviceSyncTimeStr = Utils.getStringTimeStampWithTimezoneFromDate(deviceSyncTime, TimeZone.getTimeZone(Utils.TIMEZONE_UTC));

        StringEntity entity = new StringEntity(user.toJSONObject(setting).toString(), "UTF-8");
        //RequestParams params = new RequestParams(user.toJSONObject());
        //params.setUseJsonStreamer(true);

        serverRestClient.postSync(Constants.URL.DATABALANCE_USSDCODES, entity, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Crashlytics.log(Log.DEBUG, TAG, responseString);
                Crashlytics.logException(throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Crashlytics.log(Log.DEBUG, TAG, responseString);
                try {
                    JSONObject response = new JSONObject(responseString);
                    Crashlytics.log(Log.DEBUG, TAG, response.toString());

                    boolean success = response.has(Constants.STATUS) && response.getBoolean(Constants.STATUS);
                    String msg = response.has(Constants.MESSAGE) ? response.getString(Constants.MESSAGE) : response.getString(Constants.MESSAGE);
                    if (success && response.has(Constants.DATA)) {
                        String ussd = response.getString(Constants.DATA);
                        Answers.getInstance().logCustom(new CustomEvent("Ussd Update").putCustomAttribute("ussd", ussd));

                        //ArrayList<String> list = USSDService.getUSSDCodesFromString(ussdList);

                        Setting setting = AppController.getInstance().getSetting();
                        setting.ussd = ussd;
                        AppController.getInstance().updateSetting(setting);

                    } else {
                        Crashlytics.log(Log.ERROR, TAG, "problem fetching ussd");
                    }


                } catch (JSONException e) {
                    Log.e(TAG, "", e);
                    Crashlytics.logException(e);
                }
            }
        });
        setting = AppController.getInstance().getSetting();
        return setting.ussd;
    }

    public void checkBalanceThroughSMS(
            Context c, BalanceSuccessCallback balanceSuccessCallback) {
        try {
            Crashlytics.log(Log.DEBUG, TAG, "checkBalanceThroughSMS");

            Setting setting = AppController.getInstance().getSetting();

            Integer simSlot = null;

            if (setting.simSelection == null) {
                Crashlytics.log(Log.DEBUG, TAG, "no simSlot selected");//should register first
                if (balanceSuccessCallback != null) balanceSuccessCallback.onComplete();
                return;
            }

            AppController appController = ((AppController) c.getApplicationContext());
            appController.telephonyInfo.loadInfo();

            //TODO determine simSlot from simSelection

            JSONObject telephoneData;// = simSlot == 0 ? appController.telephonyInfo.telephoneDataSIM0 : appController.telephonyInfo.telephoneDataSIM1;
            AppController.getInstance().telephonyInfo.loadInfo();
            String simId1 = TelephonyUtil.getSimId(AppController.getInstance().telephonyInfo.telephoneDataSIM0);
            String simId2 = TelephonyUtil.getSimId(AppController.getInstance().telephonyInfo.telephoneDataSIM1);

            if (appController.telephonyInfo.networkSIM0 != null && setting.simSelection.equals(simId1)) {
                //no sim in 1 try 2
                Crashlytics.log(Log.DEBUG, TAG, "sim in port 0");
                simSlot = 0;
                telephoneData = appController.telephonyInfo.telephoneDataSIM0;
            } else if (appController.telephonyInfo.networkSIM1 != null && setting.simSelection.equals(simId2)) {
                //no sim in 1 try 2
                Crashlytics.log(Log.DEBUG, TAG, "sim in port 1");
                simSlot = 1;
                telephoneData = appController.telephonyInfo.telephoneDataSIM1;
            } else {
                //no sim in 2
                Crashlytics.log(Log.DEBUG, TAG, "no sim in either port");
                if (balanceSuccessCallback != null) {
                    balanceSuccessCallback.onComplete();
                }
                return;
            }

            if (setting.ussd == null) {
                Crashlytics.log(Log.DEBUG, TAG, "no workingUSSD");

                String ussd = getUSSDCodes();//try fetch new codes
                if (ussd != null) {
                    //fetched ussd?
                    checkBalanceThroughSMS(c, balanceSuccessCallback);
                } else {
                    //failed completely
                    Crashlytics.log(Log.DEBUG, TAG, "no workingUSSD completely");
                    if (balanceSuccessCallback != null) {
                        balanceSuccessCallback.onComplete();
                    }

                }
                if (balanceSuccessCallback != null) balanceSuccessCallback.onComplete();
                return;
            }

            dataBalanceHelper.USSDtoSMSNumber(c, setting.ussd, simSlot, new DataBalanceHelper.USSDResult() {
                @Override
                public void onResult(@NonNull DataBalanceHelper.Balance bal) {

                    if (bal.rawBalance != null) { //this method works there is sim? sms feedback is good sign

                        Crashlytics.log(Log.DEBUG, TAG, "saving balance ...");
                        // String sim = TelephonyUtil.getSimSerial(c);
                        dataBalanceService.insert(bal, setting.simSelection, telephoneData);

                        if (balanceSuccessCallback != null) {
                            balanceSuccessCallback.onComplete();
                        }
                    } else {

                        setting.ussd = null;
                        AppController.getInstance().updateSetting(setting);
                        //failed completely
                        dataBalanceService.insertErrorMessage("could not fetch balance", setting.simSelection, telephoneData);
                        Answers.getInstance().logCustom(new CustomEvent("DataBalance").putCustomAttribute("Reason", "could not fetch balance"));

                        if (balanceSuccessCallback != null) {
                            balanceSuccessCallback.onComplete();
                        }
                    }
                }
            });
        } catch (Exception e) {
            Crashlytics.logException(e);
            if (balanceSuccessCallback != null) balanceSuccessCallback.onComplete();
        }
    }

    public void syncSetting(Context context) {

        AppController appController = (AppController) context.getApplicationContext();
        Setting setting = appController.getSetting();
        StringEntity entity = new StringEntity(setting.toJSONObject().toString(), "UTF-8");
        //RequestParams params = new RequestParams(user.toJSONObject());
        //params.setUseJsonStreamer(true);

        serverRestClient.postSync(Constants.URL.USER_SETTING, entity, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Crashlytics.log(Log.DEBUG, TAG, "onFailure " + responseString);
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
                    boolean changeLocationUpdateInterval = false;
                    if (success && response.has(Constants.DATA)) {
                        Setting updatedSetting = new Setting(response.getJSONObject(Constants.DATA));


                        changeLocationUpdateInterval = setting.locationUpdateInterval != updatedSetting.locationUpdateInterval;
                        setting.locationUpdateInterval = updatedSetting.locationUpdateInterval;

                        if (updatedSetting.ussd != null) {
                            setting.ussd = updatedSetting.ussd;
                        }

                        setting.databalanceCheckTime = updatedSetting.databalanceCheckTime;
                        setting.disableDatabalanceCheck = updatedSetting.disableDatabalanceCheck;

                        setting.forceUpdate = updatedSetting.forceUpdate;
                        setting.serverApi = updatedSetting.serverApi;
                        setting.disableSync = updatedSetting.disableSync;


                        AppController.getInstance().updateSetting(setting);

                        Answers.getInstance().logCustom(new CustomEvent("Setting Update").putCustomAttribute("Reason", setting.toJSONObject().toString()));

                        appController.setUSSDAlarm(setting.disableDatabalanceCheck, setting.getDatabalanceCheckTimeInMilli());

                        if (changeLocationUpdateInterval) {
                            AppController appController = (AppController) context.getApplicationContext();
                            appController.requestActivityRecognition(setting.locationUpdateInterval * 1000);
                        }

                        //check server version
                        User user = appController.getUser();
                        if (BuildConfig.VERSION_CODE < user.serverApi) {//setting.forceUpdate) {// ||
                            //  show update dialog ?
                            Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName()));
                            if (marketIntent.resolveActivity(context.getPackageManager()) != null) {
                                Intent intent = new Intent(context, UpgradeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra(UpgradeActivity.FORCE_UPDATE, setting.forceUpdate);
                                context.startActivity(intent);
                            } else {

                                Answers.getInstance().logCustom(new CustomEvent("App Update no playstore")
                                        .putCustomAttribute("Reason", "no playstore"));
                            }


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


        return;
    }

    public interface BalanceSuccessCallback {

        void onComplete();
    }

}
