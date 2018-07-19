package org.goods.living.tech.health.device.services;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.loopj.android.http.TextHttpResponseHandler;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.R;
import org.goods.living.tech.health.device.models.Setting;
import org.goods.living.tech.health.device.models.User;
import org.goods.living.tech.health.device.utils.Constants;
import org.goods.living.tech.health.device.utils.DataBalanceHelper;
import org.goods.living.tech.health.device.utils.ServerRestClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    ServerRestClient serverRestClient = new ServerRestClient(AppController.getInstance().getString(R

            .string.server_url));

    @Inject
    public RegistrationService() {
        //super(boxStore);

    }

    public User register(Context c) {

        final User user = userService.getRegisteredUser();
        user.deviceTime = new Date();

        StringEntity entity = new StringEntity(user.toJSONObject().toString(), "UTF-8");
        //RequestParams params = new RequestParams(user.toJSONObject());
        //params.setUseJsonStreamer(true);

        serverRestClient.postSync(Constants.URL.USER_CREATE, entity, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Crashlytics.log(Log.DEBUG, TAG, responseString);

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
                    Crashlytics.log(Log.DEBUG, TAG, data);

                    boolean success = response.has(Constants.STATUS) && response.getBoolean(Constants.STATUS);
                    String msg = response.has(Constants.MESSAGE) ? response.getString(Constants.MESSAGE) : response.getString(Constants.MESSAGE);
                    boolean changeLocationUpdateInterval = false;
                    if (success && response.has(Constants.DATA)) {
                        User updatedUser = new User(response.getJSONObject(Constants.DATA));

                        Answers.getInstance().logCustom(new CustomEvent("User Registration")
                                .putCustomAttribute("Reason", "androidId: "));

                        user.masterId = updatedUser.masterId;
                        changeLocationUpdateInterval = user.updateInterval != updatedUser.updateInterval;
                        user.updateInterval = updatedUser.updateInterval;
                        user.serverApi = updatedUser.serverApi;
                        user.forceUpdate = updatedUser.forceUpdate;
                        user.phone = updatedUser.phone;
                        user.country = updatedUser.country;
                        user.branch = updatedUser.branch;
                        user.name = updatedUser.name;
                        user.chvId = updatedUser.chvId;
                        user.token = updatedUser.token;

                        user.lastSync = new Date();
                        userService.insertUser(user);


                    } else {
                        Crashlytics.log(Log.ERROR, TAG, "problem registering user " + user.name);
                    }

                    if (changeLocationUpdateInterval) {
                        AppController appController = (AppController) c.getApplicationContext();
                        appController.requestLocationUpdates(user.updateInterval);
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "", e);
                    Crashlytics.logException(e);
                }
            }
        });
        return user;
    }

    public List<String> getUSSDCodes() {

        final User user = userService.getRegisteredUser();

        Setting setting = AppController.getInstance().getSetting();

        Crashlytics.log(Log.DEBUG, TAG, "fetchingUSSD: " + setting.fetchingUSSD);

        if (setting.fetchingUSSD) {

            setting.fetchingUSSD = false;
            AppController.getInstance().updateSetting(setting);
            return setting.workingUSSD0;
        }

        //  String  deviceSyncTimeStr = Utils.getStringTimeStampWithTimezoneFromDate(deviceSyncTime, TimeZone.getTimeZone(Utils.TIMEZONE_UTC));

        StringEntity entity = new StringEntity(user.toJSONObject().toString(), "UTF-8");
        //RequestParams params = new RequestParams(user.toJSONObject());
        //params.setUseJsonStreamer(true);

        serverRestClient.postSync(Constants.URL.DATABALANCE_USSDCODES, entity, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Crashlytics.log(Log.DEBUG, TAG, responseString);
                Crashlytics.logException(throwable);

                //TODO: remove
                Setting setting = AppController.getInstance().getSetting();
                setting.workingUSSD0 = DataBalanceHelper.USSDList;
                setting.workingUSSD1 = DataBalanceHelper.USSDList;//set default
                setting.fetchingUSSD = false;
                AppController.getInstance().updateSetting(setting);
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
                        String ussdList = response.getString(Constants.DATA);
                        Answers.getInstance().logCustom(new CustomEvent("Ussd Update").putCustomAttribute("ussdList", ussdList));

                        ArrayList<String> list = USSDService.getUSSDCodesFromString(ussdList);

                        Setting setting = AppController.getInstance().getSetting();
                        setting.workingUSSD0 = DataBalanceHelper.USSDList;
                        setting.workingUSSD1 = DataBalanceHelper.USSDList;
                        AppController.getInstance().updateSetting(setting);

                    } else {
                        Crashlytics.log(Log.ERROR, TAG, "problem fetching ussd");
                    }

                    Setting setting = AppController.getInstance().getSetting();
                    setting.fetchingUSSD = false;
                    AppController.getInstance().updateSetting(setting);

                } catch (JSONException e) {
                    Log.e(TAG, "", e);
                    Crashlytics.logException(e);
                    Setting setting = AppController.getInstance().getSetting();
                    setting.fetchingUSSD = false;
                    AppController.getInstance().updateSetting(setting);
                }
            }
        });
        setting = AppController.getInstance().getSetting();


        return setting.workingUSSD0;
    }

    private void checkBalanceThroughUSSD(
            Context c, int portz) {

        Crashlytics.log(Log.DEBUG, TAG, "checkBalanceThroughUSSD");

        Setting setting = AppController.getInstance().getSetting();

        AppController appController = ((AppController) c.getApplicationContext());
        appController.telephonyInfo.loadInfo();
        if (portz == 0 && appController.telephonyInfo.networkSIM1 == null) {
            portz = 1;
            //no sim in 1 try 2
            Crashlytics.log(Log.DEBUG, TAG, "no sim in port 1 try 2");
        }
        if (portz == 1 && appController.telephonyInfo.networkSIM2 == null) {
            //no sim in 2
            Crashlytics.log(Log.DEBUG, TAG, "no sim in port 2");
            return;
        }

        List<String> ussdlist = (portz == 0) ? setting.workingUSSD0 : setting.workingUSSD1;
        final int port = portz;

        String ussd = (ussdlist != null && ussdlist.size() > 0) ? ussdlist.get(0) : null;
        if (ussd != null) {
            dataBalanceHelper.dialNumber(c, ussd, port, new DataBalanceHelper.USSDResult() {
                @Override
                public void onResult(@NonNull DataBalanceHelper.Balance bal) {

                    if (bal.balance != null) { //this method works -good code and there is sim?

                        Crashlytics.log(Log.DEBUG, TAG, "saving balance ...");
                        // String sim = TelephonyUtil.getSimSerial(c);

                        JSONObject telephoneData = appController.telephonyInfo.telephoneDataSIM1;
                        if (port == 1)
                            telephoneData = appController.telephonyInfo.telephoneDataSIM2;
                        dataBalanceService.insert(bal.balance, bal.rawBalance, telephoneData);

                        //switch to line 2 if any
                        if (port == 0)
                            checkBalanceThroughUSSD(c, 1);
                        else //we r done
                            return;

                    } else {

                        ussdlist.remove(ussd);
                        AppController.getInstance().updateSetting(setting);

                        if (ussdlist.size() > 0) { //try again
                            Crashlytics.log(Log.DEBUG, TAG, "trying again balance check ...");

                            checkBalanceThroughUSSD(c, port);
                        } else {
                            //switch to line 2 if any
                            if (port == 0)
                                checkBalanceThroughUSSD(c, 1);
                            else //we r done
                                return;
                        }


                    }


                }
            });


        } else { //refetch list from server
            getUSSDCodes();
        }

    }


    public void checkBalanceThroughSMS(
            Context c, int portz, BalanceSuccessCallback balanceSuccessCallback) {

        Crashlytics.log(Log.DEBUG, TAG, "checkBalanceThroughSMS");

        Setting setting = AppController.getInstance().getSetting();

        AppController appController = ((AppController) c.getApplicationContext());
        appController.telephonyInfo.loadInfo();
        if (portz == 0 && appController.telephonyInfo.networkSIM1 == null) {
            portz = 1;
            //no sim in 1 try 2
            Crashlytics.log(Log.DEBUG, TAG, "no sim in port 1 try 2");
        }
        if (portz == 1 && appController.telephonyInfo.networkSIM2 == null) {
            //no sim in 2
            Crashlytics.log(Log.DEBUG, TAG, "no sim in port 2");
            return;
        }

        List<String> ussdlist = (portz == 0) ? setting.workingUSSD0 : setting.workingUSSD1;
        final int port = portz;

        String ussd = (ussdlist != null && ussdlist.size() > 0) ? ussdlist.get(0) : null;
        if (ussd != null) {


            dataBalanceHelper.USSDtoSMSNumber(c, ussd, port, new DataBalanceHelper.USSDResult() {
                @Override
                public void onResult(@NonNull DataBalanceHelper.Balance bal) {

                    if (bal.rawBalance != null) { //this method works there is sim? sms feedback is good sign


                        Crashlytics.log(Log.DEBUG, TAG, "saving balance ...");
                        // String sim = TelephonyUtil.getSimSerial(c);

                        JSONObject telephoneData = appController.telephonyInfo.telephoneDataSIM1;
                        if (port == 1)
                            telephoneData = appController.telephonyInfo.telephoneDataSIM2;
                        dataBalanceService.insert(bal.balance, bal.rawBalance, telephoneData);

                        if (balanceSuccessCallback != null) {
                            balanceSuccessCallback.onComplete();
                        }

                        //switch to line 2 if any
                        if (port == 0)
                            checkBalanceThroughSMS(c, 1, balanceSuccessCallback);
                        else //we r done
                            return;

                    } else {

                        ussdlist.remove(ussd);
                        AppController.getInstance().updateSetting(setting);

                        if (ussdlist.size() > 0) { //try again
                            Crashlytics.log(Log.DEBUG, TAG, "trying again balance check ...");

                            checkBalanceThroughSMS(c, port, balanceSuccessCallback);
                        } else {
                            //switch to line 2 if any
                            if (port == 0)
                                checkBalanceThroughSMS(c, 1, balanceSuccessCallback);
                            else //we r done
                                return;
                        }


                    }


                }
            });


        } else { //refetch list from server
            getUSSDCodes();
        }

    }

    public interface BalanceSuccessCallback {

        void onComplete();
    }

}
