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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatEditText;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.location.LocationRequest;
import com.hbb20.CountryCodePicker;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.R;
import org.goods.living.tech.health.device.models.Stats;
import org.goods.living.tech.health.device.models.User;
import org.goods.living.tech.health.device.services.DataBalanceService;
import org.goods.living.tech.health.device.services.StatsService;
import org.goods.living.tech.health.device.services.USSDService;
import org.goods.living.tech.health.device.services.UserService;
import org.goods.living.tech.health.device.utils.PermissionsUtils;
import org.goods.living.tech.health.device.utils.SnackbarUtil;
import org.goods.living.tech.health.device.utils.SyncAdapter;

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

    /**
     * Provides access to the Fused Location Provider API.
     */

    // UI Widgets

    private Button cancelBtn;
    private Button saveBtn;
    private TextView usernameText;
    private TextView balanceCodeTextView;
    private TextView balanceTextView;
    CountryCodePicker ccp;
    AppCompatEditText edtPhoneNumber;

    private TextView syncTextView;

    private TextView intervalTextView;

    private TextView mLocationUpdatesResultView;

    LinearLayout btnLayout;
    LinearLayout balanceLayout;

    boolean newLaunch;

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
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        edtPhoneNumber = (AppCompatEditText) findViewById(R.id.phone_number_edt);
        ccp.registerCarrierNumberEditText(edtPhoneNumber);

        balanceCodeTextView = (TextView) findViewById(R.id.balanceCodeTextView);
        balanceTextView = (TextView) findViewById(R.id.balanceTextView);

        balanceTextView.setText(getString(R.string.data_balance, "0"));

        intervalTextView = (TextView) findViewById(R.id.intervalTextView);
        btnLayout = (LinearLayout) findViewById(R.id.btnLayout);
        balanceLayout = (LinearLayout) findViewById(R.id.balanceLayout);

        newLaunch = true;
        disableSettingsEdit();

        User user = userService.getRegisteredUser();
        if (user.username == null || user.phone == null) {
            enableSettingsEdit();
        }

        loadData();

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


        if (newLaunch) {
            newLaunch = false;

            // requestLocationPermissions();
        }

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

    /**
     * Handles the Save registration button.
     */
    public void registrationSave(View view) {
        Crashlytics.log(Log.DEBUG, TAG, "registrationSave updates");

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Register User");
        builder.setMessage("Please confirm username and phone number.\n\n\n * This is a one time registration.");
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
        Crashlytics.log(Log.DEBUG, TAG, "registrationCancel ");
        loadData();
    }

    /**
     * Handles the cancel registration button.
     */
    public void checkBalance(View view) {
        Crashlytics.log(Log.DEBUG, TAG, "checkBalance ");

        //comma separated list of actions
        String balanceCode = balanceCodeTextView.getText().toString().trim();

        checkBalanceThroughUSSD(balanceCode);
        // checkBalanceThroughSMS();
    }


    public void checkBalanceThroughSMS() {
        Crashlytics.log(Log.DEBUG, TAG, "checkBalanceThroughSMS ");

        String strMessage = "bal";
        User user = userService.getRegisteredUser();

        SmsManager sms = SmsManager.getDefault();

        String ussd = USSDService.getUSSDCode(user.balanceCode);
        ussd = "100";

        sms.sendTextMessage(ussd, null, strMessage, null, null);

    }


    public void checkBalanceThroughUSSD(final String balanceCode) {

        final Context c = this;

        DataBalanceService.USSDListener USSDListener = new DataBalanceService.USSDListener() {
            @Override
            public void onUSSDReceived(String balance, String raw) {
                dataBalanceService.unbindListener();
                if (balanceTextView != null) {
                    balanceTextView.setText(getString(R.string.data_balance, balance));
                }
            }
        };

        dataBalanceService.bindListener(USSDListener);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    Answers.getInstance().logCustom(new CustomEvent("USSD Balance check")
                            .putCustomAttribute("Reason", ""));


                    if (PermissionsUtils.checkAllPermissionsGrantedAndRequestIfNot(c)) {
                        //   startService(new Intent(c.getApplicationContext(), USSDService.class));//not needed


                        String ussd = USSDService.getUSSDCode(balanceCode);
                        USSDService.dialNumber(c, ussd);

                        //well timed ones also work - but how much time ?
                        //startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + ussdCode)));
                        //        String nxt = USSDService.getNextUSSDInput();

                        //         do {

                        //             Thread.sleep(1500);


                        //             startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + nxt)));

                        //            nxt = USSDService.getNextUSSDInput();

                        //         } while (nxt != null);

                    } else {
                        // ActivityCompat.requestPermissions(c, new String[]{android.Manifest.permission.CALL_PHONE}, 1);
                        Crashlytics.log(Log.DEBUG, TAG, "USSDJobService permissions not granted yet ...");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "checkBalance ", e);
                }
            }
        }).start();
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
     * Handles the done  button.
     */
    public void minimise(View view) {

        moveTaskToBack(true);
    }

    void loadData() {

        User user = userService.getRegisteredUser();
        //if(user ==null){
        usernameText.setText(user == null ? null : user.username);
        if (user.phone != null)
            ccp.setFullNumber(user.phone);

        Long total = statsService.countRecords();
        Long synced = statsService.countSyncedRecords();
        syncTextView.setText(getString(R.string.sync_data, synced.toString(), total.toString()));

        intervalTextView.setText(getString(R.string.locationupdate_interval, "" + user.updateInterval));
        // }


        balanceCodeTextView.setText(user.balanceCode);


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


    void enableSettingsEdit() {
        btnLayout.setVisibility(View.VISIBLE);
        usernameText.setEnabled(true);
        ccp.setClickable(true);
        ccp.setCcpClickable(true);
        edtPhoneNumber.setEnabled(true);
        balanceLayout.setVisibility(View.VISIBLE);
    }

    void disableSettingsEdit() {
        btnLayout.setVisibility(View.GONE);
        usernameText.setEnabled(false);
        ccp.setClickable(false);
        ccp.setCcpClickable(false);
        edtPhoneNumber.setEnabled(false);
        balanceLayout.setVisibility(View.GONE);


    }


    void saveRegistration() {

        User user = userService.getRegisteredUser();

        String username = usernameText.getText().toString().trim();
        user.username = username.isEmpty() ? null : username;

        String phone = ccp.getFullNumberWithPlus().trim();
        //  PhoneNumberUtil pnu = PhoneNumberUtil.getInstance();

        user.phone = phone.isEmpty() ? null : phone;
        ccp.setFullNumber(user.phone);
        user.country = ccp.getSelectedCountryNameCode();

        String balanceCode = balanceCodeTextView.getText().toString().trim();
        user.balanceCode = balanceCode.isEmpty() ? null : balanceCode;


        user.recordedAt = new Date();

        if (userService.insertUser(user)) {
            SnackbarUtil.showSnack(this, "saved CHV information");
            findViewById(R.id.activity_main).requestFocus();
            if (user.username != null && user.phone != null) {
                disableSettingsEdit();
            }


            hideKeyboard(this);
        } else {
            SnackbarUtil.showSnack(this, "error saving CHV information");
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
