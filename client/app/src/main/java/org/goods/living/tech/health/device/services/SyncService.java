package org.goods.living.tech.health.device.services;

import android.util.Log;

import com.loopj.android.http.TextHttpResponseHandler;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.R;
import org.goods.living.tech.health.device.models.User;
import org.goods.living.tech.health.device.utils.Constants;
import org.goods.living.tech.health.device.utils.ServerRestClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
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


    ServerRestClient serverRestClient = new ServerRestClient(AppController.getInstance().getString(R

            .string.server_url));

    AtomicBoolean syncRunning = new AtomicBoolean(false);

    @Inject
    public SyncService() {
        //super(boxStore);

    }

    public void sync() {

        try {
            //synchronized {}
            if (syncRunning.get()) {
                Log.i(TAG, "sync running exiting...");
                return;
            }
            syncRunning.set(true);
            syncUser();
            syncStats();

            syncRunning.set(false);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            syncRunning.set(false);
        }

    }

    void syncUser() {
        try {
            final User user = userService.getRegisteredUser();


            StringEntity entity = new StringEntity(user.toJSONObject().toString(), "UTF-8");
            //RequestParams params = new RequestParams(user.toJSONObject());
            //params.setUseJsonStreamer(true);

            serverRestClient.postSync(user.masterId == null ? Constants.URL.USER_CREATE : Constants.URL.USER_UPDATE, entity, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.d(TAG, "Failed");
                    Log.d(TAG, "body " + responseString);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    Log.d("LoginActivity", "data " + responseString);
                    try {
                        JSONObject response = new JSONObject(responseString);
                        String data = response.toString();
                        Log.d(TAG, "Data : " + data);

                        // If the response is JSONObject instead of JSONArray
                        boolean success = response.has(Constants.STATUS) && response.getBoolean(Constants.STATUS);
                        String msg = response.has(Constants.MESSAGE) ? response.getString(Constants.MESSAGE) : response.getString(Constants.MESSAGE);

                        if (success && response.has(Constants.DATA)) {
                            User updatedUser = User.fromJson(response.getJSONObject(Constants.DATA));

                            user.masterId = updatedUser.masterId;
                            user.updateInterval = updatedUser.updateInterval;


                            user.lastSync = new Date();
                            userService.insertUser(user);


                        }


                    } catch (JSONException e) {
                        //  e.printStackTrace();
                        Log.e(TAG, "", e);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    void syncStats() {

    }
}
