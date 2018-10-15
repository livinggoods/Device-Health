/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.goods.living.tech.health.device.UI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.LocationRequest;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.R;
import org.goods.living.tech.health.device.models.DataBalance;
import org.goods.living.tech.health.device.models.Setting;
import org.goods.living.tech.health.device.models.Stats;
import org.goods.living.tech.health.device.models.User;
import org.goods.living.tech.health.device.receivers.LocationUpdatesBroadcastReceiver;
import org.goods.living.tech.health.device.services.DataBalanceService;
import org.goods.living.tech.health.device.services.RegistrationService;
import org.goods.living.tech.health.device.services.StatsService;
import org.goods.living.tech.health.device.services.UserService;
import org.goods.living.tech.health.device.utils.DataBalanceHelper;
import org.goods.living.tech.health.device.utils.PermissionsUtils;
import org.goods.living.tech.health.device.utils.SnackbarUtil;
import org.goods.living.tech.health.device.utils.SyncAdapter;
import org.goods.living.tech.health.device.utils.Utils;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;


/**
 * The only activity in this sample. Displays UI widgets for requesting and removing location
 * updates, and for the batched location updates that are reported.
 * <p>
 * Location updates requested through this activity continue even when the activity is not in the
 * foreground. Note: apps running on "O" devices (regardless of targetSdkVersion) may receive
 * updates less frequently than the interval specified in the {@link LocationRequest} when the app
 * is no longer in the foreground.
 */

//https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient

public class MainActivity extends FragmentActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();


    @Inject
    UserService userService;

    @Inject
    StatsService statsService;

    @Inject
    DataBalanceService dataBalanceService;

    @Inject
    RegistrationService registrationService;

    // UI Widgets

    private TextView usernameText;
    private TextView nameText;
    private TextView balanceTextView;
    TextView phoneText;


    private TextView syncTextView;

    private TextView intervalTextView;

    private TextView mLocationUpdatesResultView;

    private TextView androidIdText;

    CountDownTimer timer;


    @Inject
    DataBalanceHelper dataBalanceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  ((AppController) this.getApplicationContext()).getComponent().inject(this);
        AppController.getInstance().getComponent().inject(this);

        mLocationUpdatesResultView = (TextView) findViewById(R.id.location_updates_result);
        usernameText = (TextView) findViewById(R.id.usernameText);
        nameText = (TextView) findViewById(R.id.nameText);
        phoneText = (TextView) findViewById(R.id.phone_number);
        androidIdText = (TextView) findViewById(R.id.androidIdText);


        syncTextView = (TextView) findViewById(R.id.syncTextView);


        balanceTextView = (TextView) findViewById(R.id.balanceTextView);

        balanceTextView.setText(getString(R.string.data_balance, "0", "", "", ""));

        intervalTextView = (TextView) findViewById(R.id.intervalTextView);

        WindowManager.LayoutParams settings = getWindow().getAttributes();


        // Crashlytics.getInstance().crash(); // Force a crash

        // Utils.makeGooglePlayServicesAvailable(this);


        // ((AppController) this.getApplicationContext()).requestLocationUpdates(this);
        AppController.getInstance().requestLocationUpdates(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Crashlytics.log(Log.DEBUG, TAG, "onResume");
        AppController.inBackground = false;
        loadData();

    }

    @Override
    protected void onPause() {
        super.onPause();
        Crashlytics.log(Log.DEBUG, TAG, "onPause");

    }


    @Override
    protected void onStop() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        Crashlytics.log(Log.DEBUG, TAG, "onSharedPreferenceChanged " + s);
        //  if (s.equals(Utils.KEY_LOCATION_UPDATES_RESULT)) {

        //  } else if (s.equals(Utils.KEY_LOCATION_UPDATES_REQUESTED)) {
        //     updateButtonsState(Utils.getRequestingLocationUpdates(this));
        //   }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Crashlytics.log(Log.DEBUG, TAG, "onActivityResult ...");
    }

    /**
     * Handles the triggerSync  button.
     */
    public void triggerSync(View view) {
        Crashlytics.log(Log.DEBUG, TAG, "triggerSync ");

        SnackbarUtil.showSnack(this, "Performing sync");

        Utils.showProgressDialog(this);

        SyncAdapter.performSync();

        Handler handler = new Handler(Looper.getMainLooper());
        final Runnable r = new Runnable() {
            public void run() {
                Utils.dismissProgressDialog();
                loadData();
            }
        };
        handler.postDelayed(r, 5000);
    }

    /**
     * Handles the checkBalance  button.
     */
    public void checkBalance(View view) {

        Context c = this;

        Utils.showProgressDialog(this);

        Utils.getHandlerThread().post(new Runnable() {
            @Override
            public void run() {


                //    registrationService.checkBalanceThroughUSSD(c,0);
                registrationService.checkBalanceThroughSMS(c, new RegistrationService.BalanceSuccessCallback() {
                    @Override
                    public void onComplete() {

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Utils.dismissProgressDialog();
                                    List<DataBalance> list = dataBalanceService.getLatestRecords(1l);
                                    updateUI(list);
                                } catch (Exception e) {
                                    Crashlytics.logException(e);

                                }
                            }
                        });
                    }
                });


            }
        });


    }


    void updateUI(List<DataBalance> list) {

        Crashlytics.log(Log.DEBUG, TAG, "onFinish checkBalance");
        Utils.dismissProgressDialog();
        DataBalance dataBalance = list.size() > 0 ? list.get(0) : null;

        if (dataBalance != null)
            if (balanceTextView != null) {

                String expirely = Utils.getStringDateFromDate(dataBalance.expiryDate);

                balanceTextView.setText(getString(R.string.data_balance, dataBalance.balance, expirely, dataBalance.balanceMessage));
            }

    }


    /**
     * Handles the checkLocation  button.
     */
    public void checkLocation(View view) {

        Activity c = this;

        AppController.getInstance().requestActivityRecognition(AppController.getInstance().getSetting().locationUpdateInterval * 1000);
        boolean locationOn = PermissionsUtils.isLocationOn(c);


        if (!locationOn) {
            Utils.dismissProgressDialog();
            SnackbarUtil.showSnack(c, "First Enable Location on device");
            return;
        }

        Utils.showProgressDialog(this);

        Utils.getHandlerThread().post(new Runnable() {
            @Override
            public void run() {
                try {

                    AppController appController = AppController.getInstance();
                    List<Stats> l = statsService.getLatestRecords(1l);
                    Location oldLocation = l.size() > 0 ? LocationUpdatesBroadcastReceiver.locationFromStats(l.get(0)) : null;


                    Location loc = appController.getLastLocation();

                    Location location = LocationUpdatesBroadcastReceiver.getBestLastLocation(oldLocation, loc);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Utils.dismissProgressDialog();
                            try {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    // Logic to handle location object
                                    String result = "Current Location Latitude is " +
                                            location.getLatitude() + "\n" +
                                            "Current location Longitude is " + location.getLongitude()
                                            + "Time " + new Date(location.getTime());

                                    Crashlytics.log(Log.DEBUG, TAG, result);

                                    Float bright = Utils.getBrightness(c, getWindow());

                                    Setting setting = AppController.getInstance().getSetting();
                                    setting.brightness = bright != null ? bright.doubleValue() : null;
                                    AppController.getInstance().updateSetting(setting);


                                    Integer batteryLevel = Utils.getBatteryPercentage(c);
                                    statsService.insertLocation(location, setting.brightness, batteryLevel);
                                    loadData();

                                } else {
                                    SnackbarUtil.showSnack(c, "Could not get location");

                                }
                            } catch (Exception e) {
                                Crashlytics.logException(e);
                            }
                        }

                    });

                } catch (Exception e) {
                    Log.e(TAG, "", e);
                    Crashlytics.logException(e);
                    Utils.dismissProgressDialog();
                }

            }
        });

    }

    /**
     * Handles the done  button.
     */
    public void minimise(View view) {

        moveTaskToBack(true);
    }

    void loadData() {

        Setting setting = AppController.getInstance().getSetting();
        User user = userService.getRegisteredUser();

        if (user.masterId == null || setting.simSelection == null) {
            // enableSettingsEdit();
            Intent intent = new Intent(this, RegisterActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // intent.putExtra("forceUpdate", forceUpdate);
            this.startActivity(intent);
        }

        //if(user ==null){
        usernameText.setText(user == null ? null : user.username);
        nameText.setText(user == null ? null : user.name);
        phoneText.setText(user == null ? null : user.phone);
        androidIdText.setText(user == null ? null : user.androidId);

        List<DataBalance> l = dataBalanceService.getLatestRecords(1l);
        updateUI(l);

        Long total = statsService.countRecords();
        Long synced = statsService.countSyncedRecords();
        syncTextView.setText(getString(R.string.sync_data, synced.toString(), total.toString()));

        intervalTextView.setText(getString(R.string.locationupdate_interval, "" + AppController.getInstance().getSetting().locationUpdateInterval));
        // }


        //load latest locs
        List<Stats> list = statsService.getLatestRecords(50l);
        // long count = statsService.getStatsCount();
        String data = "";
        for (Stats stats : list) {
            data += "" + DateFormat.format("MM/dd h:m:s", stats.recordedAt); //new Date(TimeinMilliSeccond)
            data += " lat: " + stats.latitude;
            data += " lon: " + stats.longitude;
            data += " acu: " + stats.accuracy;
            data += "\n";
        }

        mLocationUpdatesResultView.setText(data);
    }


}
