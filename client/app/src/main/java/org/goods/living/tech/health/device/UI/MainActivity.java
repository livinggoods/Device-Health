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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.LocationRequest;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.R;
import org.goods.living.tech.health.device.models.Stats;
import org.goods.living.tech.health.device.models.User;
import org.goods.living.tech.health.device.services.StatsService;
import org.goods.living.tech.health.device.services.UserService;
import org.goods.living.tech.health.device.utils.AuthenticatorService;
import org.goods.living.tech.health.device.utils.PermissionsUtils;
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

    /**
     * Provides access to the Fused Location Provider API.
     */

    // UI Widgets

    private Button cancelBtn;
    private Button saveBtn;
    private TextView usernameText;
    private TextView syncTextView;

    private TextView intervalTextView;

    private TextView mLocationUpdatesResultView;

    LinearLayout btnLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppController.getInstance().getComponent().inject(this);

        mLocationUpdatesResultView = (TextView) findViewById(R.id.location_updates_result);
        cancelBtn = (Button) findViewById(R.id.cancelBtn);
        saveBtn = (Button) findViewById(R.id.saveBtn);
        usernameText = (TextView) findViewById(R.id.usernameText);
        syncTextView = (TextView) findViewById(R.id.syncTextView);
        intervalTextView = (TextView) findViewById(R.id.intervalTextView);
        btnLayout = (LinearLayout) findViewById(R.id.btnLayout);

        disableSettingsEdit();

        User user = userService.getRegisteredUser();
        if (user.username == null) {
            enableSettingsEdit();
        }

        loadData();

        // Create your sync account
        AuthenticatorService.createSyncAccount(this);

        // Perform a manual sync by calling this:
        SyncAdapter.performSync();


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
        PermissionsUtils.checkAndRequestPermissions(this, userService);

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

        Log.i(TAG, "onSharedPreferenceChanged " + s);
        //  if (s.equals(Utils.KEY_LOCATION_UPDATES_RESULT)) {

        //  } else if (s.equals(Utils.KEY_LOCATION_UPDATES_REQUESTED)) {
        //     updateButtonsState(Utils.getRequestingLocationUpdates(this));
        //   }
    }

    /**
     * Handles the Save registration button.
     */
    public void registrationSave(View view) {
        Log.i(TAG, "registrationSave updates");

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Register Details");
        builder.setMessage("This is a one time registration.\n Confirm save");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                saveRegistration();
            }
        });
        // builder.setNegativeButton(android.R.string.no, null);
        // builder.setCancelable(false);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();


    }

    /**
     * Handles the cancel registration button.
     */
    public void registrationCancel(View view) {
        Log.i(TAG, "registrationCancel ");
        loadData();
    }

    /**
     * Handles the triggerSync  button.
     */
    public void triggerSync(View view) {
        Log.i(TAG, "triggerSync ");

        Utils.showSnack(this, "Performing sync");
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
     * Handles the done  button.
     */
    public void minimise(View view) {
        moveTaskToBack(true);
    }

    void loadData() {

        //if 1st run - no user record exists.
        User user = userService.getRegisteredUser();
        //if(user ==null){
        usernameText.setText(user == null ? null : user.username);

        Long total = statsService.countRecords();
        Long synced = statsService.countSyncedRecords();
        syncTextView.setText("Synced data: " + synced + "/" + total);
        intervalTextView.setText("Location Update interval (seconds): " + (user == null ? ("" + PermissionsUtils.UPDATE_INTERVAL) : "" + user.updateInterval));
        // }


        //load latest locs
        List<Stats> list = statsService.getLatestStats(500l);
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


    void enableSettingsEdit() {
        btnLayout.setVisibility(View.VISIBLE);
        usernameText.setEnabled(true);
    }

    void disableSettingsEdit() {
        btnLayout.setVisibility(View.GONE);
        usernameText.setEnabled(false);

    }


    void saveRegistration() {

        User user = userService.getRegisteredUser();

        String username = usernameText.getText().toString().trim();
        user.username = username.isEmpty() ? null : username;
        user.recordedAt = new Date();

        if (userService.insertUser(user)) {
            Utils.showSnack(this, "saved CHV information");
            findViewById(R.id.activity_main).requestFocus();
            if (user.username != null) {
                disableSettingsEdit();
            }


            hideKeyboard(this);
        } else {
            Utils.showSnack(this, "error saving CHV information");
        }
    }


    public void hideKeyboard(Activity activity) {
        if (activity != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (activity.getCurrentFocus() != null && inputManager != null) {
                inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
                inputManager.showSoftInputFromInputMethod(activity.getCurrentFocus().getWindowToken(), 0);
            }
        }
    }
}
