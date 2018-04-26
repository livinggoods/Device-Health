package org.goods.living.tech.health.device.services;

import android.util.Log;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.R;
import org.goods.living.tech.health.device.models.User;
import org.goods.living.tech.health.device.utils.Constants;
import org.goods.living.tech.health.device.utils.ServerRestClient;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;

import cz.msebera.android.httpclient.Header;

@Singleton
public class SyncService extends BaseService {


    @Inject
    StatsService statsService;

    @Inject
    UserService userService;


    ServerRestClient serverRestClient = new ServerRestClient(AppController.getInstance().getString(R

            .string.server_url));

    boolean syncRunning;

    @Inject
    public SyncService() {
        //super(boxStore);

    }

    synchronized boolean isSyncRunning() {
        return syncRunning;
    }

    synchronized void setSyncRunning(boolean syncRunning) {
        this.syncRunning = syncRunning;
    }

    public void sync() {

        try {

            if (isSyncRunning()) {
                Log.i(TAG, "sync running exiting...");
                return;
            }
            setSyncRunning(true);
            syncUser();
            syncStats();

            setSyncRunning(false);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            setSyncRunning(false);
        }

    }

    void syncUser() {
        User user = userService.getRegisteredUser();


        // HashMap<String, String> paramMap = new HashMap<String, String>();
        //paramMap.put("key", "value");
        RequestParams params = new RequestParams(user.toHashMap());

        serverRestClient.postSync(user.masterId == null ? Constants.URL.USER_CREATE : Constants.URL.USER_UPDATE, params, new TextHttpResponseHandler() {
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


                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Log.i(TAG, "hit here after sync results");
    }

    void syncStats() {

    }
}
