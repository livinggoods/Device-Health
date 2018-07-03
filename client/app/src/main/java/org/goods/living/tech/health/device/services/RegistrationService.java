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
import org.goods.living.tech.health.device.utils.PermissionsUtils;
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

                    boolean success = response.has(Constants.STATUS) && response.getBoolean(Constants.STATUS);
                    String msg = response.has(Constants.MESSAGE) ? response.getString(Constants.MESSAGE) : response.getString(Constants.MESSAGE);
                    boolean changeLocationUpdateInterval = false;
                    if (success && response.has(Constants.DATA)) {
                        User updatedUser = new User(response.getJSONObject(Constants.DATA));

                        Answers.getInstance().logCustom(new CustomEvent("User Registration")
                                .putCustomAttribute("Reason", "androidId: " + updatedUser.androidId + " username: " + updatedUser.username));

                        user.masterId = updatedUser.masterId;
                        changeLocationUpdateInterval = user.updateInterval != updatedUser.updateInterval;
                        user.updateInterval = updatedUser.updateInterval;
                        user.serverApi = updatedUser.serverApi;
                        user.forceUpdate = updatedUser.forceUpdate;
                        user.phone = updatedUser.phone;
                        user.token = updatedUser.token;

                        user.lastSync = new Date();
                        userService.insertUser(user);


                    } else {
                        Crashlytics.log(Log.ERROR, TAG, "problem registering user");
                    }

                    if (changeLocationUpdateInterval) {
                        PermissionsUtils.requestLocationUpdates(c, user.updateInterval);
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


        //  String  deviceSyncTimeStr = Utils.getStringTimeStampWithTimezoneFromDate(deviceSyncTime, TimeZone.getTimeZone(Utils.TIMEZONE_UTC));

        StringEntity entity = new StringEntity(user.toJSONObject().toString(), "UTF-8");
        //RequestParams params = new RequestParams(user.toJSONObject());
        //params.setUseJsonStreamer(true);

        serverRestClient.postSync(Constants.URL.USER_UPDATE, entity, new TextHttpResponseHandler() {
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
                        String ussdList = response.getString(Constants.DATA);
                        Answers.getInstance().logCustom(new CustomEvent("Ussd Update").putCustomAttribute("ussdList", ussdList));

                        ArrayList<String> list = USSDService.getUSSDCodesFromString(ussdList);

                        Setting setting = AppController.getInstance().getSetting();

                        setting.workingUSSD = list;
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


        return AppController.getInstance().getSetting().workingUSSD;
    }

    public void checkBalanceThroughUSSD(
            Context c) {

        Setting setting = AppController.getInstance().getSetting();

        if (setting.workingUSSD != null && setting.workingUSSD.size() > 0) {
            String ussd = setting.workingUSSD.get(0);
            dataBalanceHelper.dialNumber(c, ussd, new DataBalanceHelper.USSDResult() {
                @Override
                public void onResult(@NonNull List<DataBalanceHelper.Balance> list) {


                    boolean USSDWorks = false;
                    for (DataBalanceHelper.Balance b : list) {

                        if (b.balance != null) { //this method works -good code and there is sim?
                            USSDWorks = true;
                            Crashlytics.log(Log.DEBUG, TAG, "saving balance ...");
                            dataBalanceService.insert(b.balance, b.rawBalance);

                        }


                    }
                    if (!USSDWorks) {//none works delete entry
                        setting.workingUSSD.remove(ussd);
                        AppController.getInstance().updateSetting(setting);

                        if (setting.workingUSSD.size() > 0) { //try again
                            Crashlytics.log(Log.DEBUG, TAG, "trying again balance check ...");

                            checkBalanceThroughUSSD(c);
                        } else { //refetch list from server
                            getUSSDCodes();
                        }


                    }
                    ;


                }
            });


        } else { //refetch list from server
            getUSSDCodes();
        }

    }
}
