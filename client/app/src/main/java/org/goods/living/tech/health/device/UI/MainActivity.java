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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.BuildConfig;
import org.goods.living.tech.health.device.R;
import org.goods.living.tech.health.device.models.Stats;
import org.goods.living.tech.health.device.models.User;
import org.goods.living.tech.health.device.services.LocationUpdatesIntentService;
import org.goods.living.tech.health.device.services.StatsService;
import org.goods.living.tech.health.device.services.UserService;
import org.goods.living.tech.health.device.utils.AuthenticatorService;
import org.goods.living.tech.health.device.utils.LocationUpdatesBroadcastReceiver;
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
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL = 20; // Every 60 seconds.

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    private static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2; // Every 30 seconds

    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
   // private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 5; // Every 5 minutes.

    private static final long MAX_WAIT_RECORDS =  2; // Every 5 items

    LocationRequest mLocationRequest;

    @Inject
    UserService userService;

    @Inject
    StatsService statsService;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;
    // UI Widgets

    private Button cancelBtn;
    private Button saveBtn;
    private TextView chpText;
    private TextView phoneText;

    private TextView intervalText;

    private TextView mLocationUpdatesResultView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppController.getInstance().getComponent().inject(this);

        mLocationUpdatesResultView = (TextView) findViewById(R.id.location_updates_result);
        cancelBtn = (Button) findViewById(R.id.cancelBtn);
        saveBtn = (Button) findViewById(R.id.saveBtn);
        chpText = (TextView) findViewById(R.id.chpText);
        phoneText = (TextView) findViewById(R.id.phoneText);
;
        intervalText = (TextView) findViewById(R.id.intervalText);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = createLocationRequest();

        createUserOnFirstRun();

        loadData();

        // Create your sync account
        AuthenticatorService.createSyncAccount(this);

        // Perform a manual sync by calling this:
        SyncAdapter.performSync();
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

        // Check if the user revoked runtime permissions.
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            requestLocationUpdates();

            checkLocationEnabled();
        }
        loadData();
    }

    @Override
    protected void onStop() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        // Note: apps running on "O" devices (regardless of targetSdkVersion) may receive updates
        // less frequently than this interval when the app is no longer in the foreground.


        long updateInterval = UPDATE_INTERVAL * 1000;
        long fastestUpdateInterval = updateInterval / 2;


        User user = userService.getRegisteredUser();
        if (user != null) {
            updateInterval = user.updateInterval * 1000;
            fastestUpdateInterval = updateInterval / 2;
        }


        mLocationRequest.setInterval(updateInterval);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(fastestUpdateInterval);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.

        long maxWaitTime = updateInterval * MAX_WAIT_RECORDS;
        mLocationRequest.setMaxWaitTime(maxWaitTime);
        return mLocationRequest;
    }

    private PendingIntent getPendingIntent() {
        // Note: for apps targeting API level 25 ("Nougat") or lower, either
        // PendingIntent.getService() or PendingIntent.getBroadcast() may be used when requesting
        // location updates. For apps targeting API level O, only
        // PendingIntent.getBroadcast() should be used. This is due to the limits placed on services
        // started in the background in "O".

        // TODO(developer): uncomment to use PendingIntent.getService().
      //  Intent intent = new Intent(this, LocationUpdatesIntentService.class);
      //  intent.setAction(LocationUpdatesIntentService.ACTION_PROCESS_UPDATES);
      //  return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

          Intent intent = new Intent(this, LocationUpdatesBroadcastReceiver.class);
           intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
          return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.activity_main),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("Ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                requestLocationUpdates();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
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
     * Handles the Request Updates button and requests start of location updates.
     */
    public void requestLocationUpdates() {
        try {
            Log.i(TAG, "Starting location updates");
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());
            checkLocationEnabled();

        } catch (SecurityException e) {

            e.printStackTrace();
        }
    }

    /**
     * Handles the Remove Updates button, and requests removal of location updates.
     */
    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        mFusedLocationClient.removeLocationUpdates(getPendingIntent());
    }

    void showSnack(String text) {
        Snackbar.make(
                findViewById(R.id.activity_main),
                text,//R.string.permission_denied_explanation,
                Snackbar.LENGTH_SHORT)
                .setAction(R.string.settings, null)
                .show();
    }

    /**
     * Handles the Save registration button.
     */
    public void registrationSave(View view) {
        Log.i(TAG, "registrationSave updates");
        saveRegistration();


    }

    /**
     * Handles the cancel registration button.
     */
    public void registrationCancel(View view) {
        Log.i(TAG, "registrationCancel ");
        loadData();
    }



    public void minimise(View view) {
        moveTaskToBack(true);
    }

    void loadData() {

        //if 1st run - no user record exists.
        User user = userService.getRegisteredUser();
        //if(user ==null){
        chpText.setText(user == null ? null : user.chpId);
        phoneText.setText(user == null ? null : user.phoneNumber);
        intervalText.setText(user == null ? null : "" + user.updateInterval);
        // }


        //load latest locs
        List<Stats> list = statsService.getLatestStats();
        long count = statsService.getStatsCount();
        String data = "count :" + count + " \n" +
                "";
        for (Stats stats : list) {
            data += " tim: " + DateFormat.format("MM/dd m:s",stats.time); //new Date(TimeinMilliSeccond)
            data += " lat: " + stats.latitude;
            data += " lon: " + stats.longitude;
            data += "\n";
        }

        mLocationUpdatesResultView.setText(data);
    }

    void createUserOnFirstRun() {

        //if 1st run - no user record exists.
        User user = userService.getRegisteredUser();
        if (user == null) {
            user = new User();
            user.updateInterval = UPDATE_INTERVAL;
            //add device info
            String androidId = Utils.getAndroidId(this);
            user.androidId = androidId;
            user.createdAt = new Date();
            if (userService.insertUser(user)) {
                showSnack("created user settings");
            } else {
                showSnack("error creating user information");
            }
        }

    }


    void saveRegistration() {

        User user = userService.getRegisteredUser();

        user.chpId = chpText.getText().toString().trim();
        user.phoneNumber = chpText.getText().toString().trim();
        Long interval = Long.valueOf(intervalText.getText().toString());
        user.updateInterval = interval==null?UPDATE_INTERVAL:interval;

        if (userService.insertUser(user)) {
            showSnack("saved CHV information");
            findViewById(R.id.activity_main).requestFocus();

            hideKeyboard(this);
        } else {
            showSnack("error saving CHV information");
        }
    }


    public void checkLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsProviderEnabled, isNetworkProviderEnabled;
        isGpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!Utils.isLocationOn(this)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Permission");
            builder.setMessage("The app needs location permissions. Please grant this permission to continue using the features of the app.");
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            // builder.setNegativeButton(android.R.string.no, null);
            builder.setCancelable(false);
            builder.setNegativeButton(null, null);
            builder.show();
        }
    }


    public  void hideKeyboard(Activity activity) {
        if (activity != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (activity.getCurrentFocus() != null && inputManager != null) {
                inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
                inputManager.showSoftInputFromInputMethod(activity.getCurrentFocus().getWindowToken(), 0);
            }
        }
    }
}
