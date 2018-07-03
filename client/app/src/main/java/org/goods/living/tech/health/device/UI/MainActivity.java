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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatEditText;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.LocationRequest;
import com.hbb20.CountryCodePicker;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.R;
import org.goods.living.tech.health.device.models.DataBalance;
import org.goods.living.tech.health.device.models.Stats;
import org.goods.living.tech.health.device.models.User;
import org.goods.living.tech.health.device.services.DataBalanceService;
import org.goods.living.tech.health.device.services.RegistrationService;
import org.goods.living.tech.health.device.services.StatsService;
import org.goods.living.tech.health.device.services.UserService;
import org.goods.living.tech.health.device.utils.DataBalanceHelper;
import org.goods.living.tech.health.device.utils.SnackbarUtil;
import org.goods.living.tech.health.device.utils.SyncAdapter;
import org.goods.living.tech.health.device.utils.Utils;

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
    CountryCodePicker ccp;
    AppCompatEditText edtPhoneNumber;

    private TextView syncTextView;

    private TextView intervalTextView;

    private TextView mLocationUpdatesResultView;

    CountDownTimer timer;


    @Inject
    DataBalanceHelper dataBalanceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppController.getInstance().getComponent().inject(this);

        mLocationUpdatesResultView = (TextView) findViewById(R.id.location_updates_result);
        usernameText = (TextView) findViewById(R.id.usernameText);
        nameText = (TextView) findViewById(R.id.nameText);

        syncTextView = (TextView) findViewById(R.id.syncTextView);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        edtPhoneNumber = (AppCompatEditText) findViewById(R.id.phone_number_edt);
        ccp.registerCarrierNumberEditText(edtPhoneNumber);

        balanceTextView = (TextView) findViewById(R.id.balanceTextView);

        balanceTextView.setText(getString(R.string.data_balance, "0"));

        intervalTextView = (TextView) findViewById(R.id.intervalTextView);

        // disableSettingsEdit
        ccp.setClickable(false);
        ccp.setCcpClickable(false);
        edtPhoneNumber.setEnabled(false);
        edtPhoneNumber.setClickable(false);

        User user = userService.getRegisteredUser();
        //   if (user.masterId == null) {
        // enableSettingsEdit();
        //        Intent intent = new Intent(this, RegisterActivity.class);
        //        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //  intent.putExtra("forceUpdate", forceUpdate);
        //        this.startActivity(intent);
        //   }
        //  checkBalanceThroughUSSD("*100*6*6*2#");
        // Crashlytics.getInstance().crash(); // Force a crash

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

        loadData();

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
        SyncAdapter.performSync();

        Handler handler = new Handler(Looper.getMainLooper());
        final Runnable r = new Runnable() {
            public void run() {
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

        Utils.getHandlerThread().post(new Runnable() {
            @Override
            public void run() {

                registrationService.checkBalanceThroughUSSD(c);

                if (timer != null) timer.cancel();
                timer = new CountDownTimer(DataBalanceHelper.USSD_LIMIT * 2000, DataBalanceHelper.USSD_LIMIT * 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        Crashlytics.log(Log.DEBUG, TAG, "tick tock ... waiting for ussd " + millisUntilFinished);

                    }

                    @Override
                    public void onFinish() {
                        Crashlytics.log(Log.DEBUG, TAG, "onFinish checkBalance");

                        List<DataBalance> list = dataBalanceService.getLatestRecords(1l);
                        DataBalance dataBalance = list.size() > 0 ? list.get(0) : null;
                        if (dataBalance != null)
                            if (balanceTextView != null) {
                                balanceTextView.setText(getString(R.string.data_balance, dataBalance.balance));
                            }
                        timer = null;
                    }
                };
                timer.start();
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

        User user = userService.getRegisteredUser();
        //if(user ==null){
        usernameText.setText(user == null ? null : user.username);
        nameText.setText(user == null ? null : user.name);
        if (user.phone != null)
            ccp.setFullNumber(user.phone);

        Long total = statsService.countRecords();
        Long synced = statsService.countSyncedRecords();
        syncTextView.setText(getString(R.string.sync_data, synced.toString(), total.toString()));

        intervalTextView.setText(getString(R.string.locationupdate_interval, "" + user.updateInterval));
        // }


        //load latest locs
        List<Stats> list = statsService.getLatestRecords(50l);
        long count = statsService.getStatsCount();
        String data = "count :" + count + " \n" +
                "";
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
