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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.LocationRequest;
import com.hbb20.CountryCodePicker;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.R;
import org.goods.living.tech.health.device.models.DataBalance;
import org.goods.living.tech.health.device.models.Setting;
import org.goods.living.tech.health.device.models.User;
import org.goods.living.tech.health.device.services.DataBalanceService;
import org.goods.living.tech.health.device.services.RegistrationService;
import org.goods.living.tech.health.device.services.StatsService;
import org.goods.living.tech.health.device.services.SyncService;
import org.goods.living.tech.health.device.services.UserService;
import org.goods.living.tech.health.device.utils.SnackbarUtil;
import org.goods.living.tech.health.device.utils.Utils;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.SlideFragment;


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

public class RegisterUserFragment extends SlideFragment {

    private final String TAG = getClass().getSimpleName();


    @Inject
    UserService userService;

    @Inject
    StatsService statsService;

    @Inject
    DataBalanceService dataBalanceService;
    @Inject
    RegistrationService registrationService;

    @Inject
    SyncService syncService;

    private Button registerBtn;
    private TextView usernameText;
    private TextView balanceTextView;
    CountryCodePicker ccp;
    AppCompatEditText edtPhoneNumber;
    TextView msgTextView;

    RadioButton radioButton1;
    RadioButton radioButton2;


    public interface RegistrationFlow {

        public void onDoRegister(View view);

        public void onDoBalance(View view);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_slide1, container, false);

        AppController.getInstance().getComponent().inject(this);


        registerBtn = (Button) view.findViewById(R.id.registerBtn);
        usernameText = (TextView) view.findViewById(R.id.usernameText);
        msgTextView = (TextView) view.findViewById(R.id.msgTextView);
        ccp = (CountryCodePicker) view.findViewById(R.id.ccp);
        edtPhoneNumber = (AppCompatEditText) view.findViewById(R.id.phone_number_edt);
        ccp.registerCarrierNumberEditText(edtPhoneNumber);

        radioButton1 = (RadioButton) view.findViewById(R.id.radioButton);
        radioButton2 = (RadioButton) view.findViewById(R.id.radioButton2);

        balanceTextView = (TextView) view.findViewById(R.id.balanceTextView);

        balanceTextView.setText(getString(R.string.data_balance, "0", "", "", ""));


        loadData();
        return view;
    }

    @Override
    public int backgroundColor() {
        return R.color.custom_slide_background;
    }

    @Override
    public int buttonsColor() {
        return R.color.custom_slide_buttons;
    }

    @Override
    public boolean canMoveFurther() {

        User user = userService.getRegisteredUser();
        //return user.masterId != null;


        Setting setting = AppController.getInstance().getSetting();
        boolean registered = user.masterId != null && !setting.fetchingUSSD;


        if (registered) {
            List<DataBalance> l = dataBalanceService.getLatestRecords(1l);
            DataBalance dataBalance = l.size() > 0 ? l.get(0) : null;
            if (dataBalance == null) {
                Crashlytics.log("Could not retrieve balance");
                return false;
            }
        } else {
            Crashlytics.log("User not registered");
        }

        return registered;
    }

    @Override
    public String cantMoveFurtherErrorMessage() {

        User user = userService.getRegisteredUser();
        if (user.masterId != null) {
            return "Please test balance checking to continue";
        }
        return "please register a valid chv to continue";//getString(R.string.error_message);
    }

    void loadData() {
        Utils.getHandlerThread().post(new Runnable() {
            @Override
            public void run() {
                User user = userService.getRegisteredUser();
                List<DataBalance> l = dataBalanceService.getLatestRecords(1l);

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //this runs on the UI thread


                        usernameText.setText(user.username);
                        if (user.phone != null)
                            ccp.setFullNumber(user.phone);


                        DataBalance dataBalance = l.size() > 0 ? l.get(0) : null;

                        if (dataBalance != null)
                            if (balanceTextView != null) {
                                balanceTextView.setText(getString(R.string.data_balance, dataBalance.sim, dataBalance.balance, dataBalance.expiryDate, dataBalance.balanceMessage));
                            }
                    }
                });
            }
        });

    }

    /**
     * Handles the Save registration button.
     */
    public void onDoRegister() {
        Crashlytics.log(Log.DEBUG, TAG, "onDoRegister");

        final AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle("Register CHV");
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
     * Handles the Balance button.
     */
    public void onDoBalance() {
        Crashlytics.log(Log.DEBUG, TAG, "onDoBalance");

        checkBalance();

    }

    public void checkBalance() {

        Context c = this.getActivity();

        Utils.showProgressDialog(c);

        Utils.getHandlerThread().post(new Runnable() {
            @Override
            public void run() {

                //    registrationService.checkBalanceThroughUSSD(c,0);
                registrationService.checkBalanceThroughSMS(c, 0, new RegistrationService.BalanceSuccessCallback() {
                    @Override
                    public void onComplete() {
                        Utils.dismissProgressDialog();
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                //this runs on the UI thread
                                List<DataBalance> l = dataBalanceService.getLatestRecords(1l);
                                DataBalance dataBalance = l.size() > 0 ? l.get(0) : null;

                                if (dataBalance != null && balanceTextView != null) {
                                    balanceTextView.setText(getString(R.string.data_balance, dataBalance.sim, dataBalance.balance, dataBalance.expiryDate, dataBalance.balanceMessage));
                                }
                            }
                        });


                    }
                });

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //this runs on the UI thread

                    }
                });
            }
        });


    }

    void saveRegistration() {

        User user = userService.getRegisteredUser();
        String username = usernameText.getText().toString().trim();
        user.username = username.isEmpty() ? null : username;
        String phone = "";
        try {
            if (!ccp.getFullNumber().trim().isEmpty())
                phone = ccp.getFullNumberWithPlus().trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //  PhoneNumberUtil pnu = PhoneNumberUtil.getInstance();
        String numOnly = phone.replace(ccp.getSelectedCountryCodeWithPlus(), "");
        user.phone = numOnly.isEmpty() ? null : phone;
        user.country = ccp.getSelectedCountryNameCode();

        user.recordedAt = new Date();
        userService.insertUser(user);

        Setting setting = AppController.getInstance().getSetting();
        setting.simSlot = 0;
        setting.network = AppController.getInstance().telephonyInfo.networkSIM0;
        if (radioButton2.isChecked()) {
            setting.simSlot = 1;
            setting.network = AppController.getInstance().telephonyInfo.networkSIM1;
        }


        AppController.getInstance().updateSetting(setting);


        hideKeyboard(this.getActivity());
        SnackbarUtil.showSnack(this.getActivity(), "saving CHV information ... ");


        MaterialIntroActivity c = (MaterialIntroActivity) this.getActivity();

        Utils.showProgressDialog(c);

        Utils.getHandlerThread().post(new Runnable() {
            @Override
            public void run() {

                User updatedUser = registrationService.register(c);

                if (updatedUser.masterId != null) {
                    syncService.syncSetting();

                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //this runs on the UI thread
                        Utils.dismissProgressDialog();
                        if (updatedUser.masterId != null) {
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            //  intent.putExtra("forceUpdate", forceUpdate);
                            getActivity().startActivity(intent);

                        }


                    }
                });
            }
        });


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