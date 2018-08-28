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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.LocationRequest;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.R;
import org.goods.living.tech.health.device.services.DataBalanceService;
import org.goods.living.tech.health.device.services.StatsService;
import org.goods.living.tech.health.device.services.UserService;

import javax.annotation.Nullable;
import javax.inject.Inject;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.SlideFragmentBuilder;
import agency.tango.materialintroscreen.animations.IViewTranslation;


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

public class RegisterActivity extends MaterialIntroActivity implements RegisterUserFragment.RegistrationFlow {

    private static final String TAG = RegisterActivity.class.getSimpleName();


    @Inject
    UserService userService;

    @Inject
    StatsService statsService;

    @Inject
    DataBalanceService dataBalanceService;

    RegisterUserFragment registerUserFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableLastSlideAlphaExitTransition(true);

        getBackButtonTranslationWrapper()
                .setEnterTranslation(new IViewTranslation() {
                    @Override
                    public void translate(View view, @FloatRange(from = 0, to = 1.0) float percentage) {
                        view.setAlpha(percentage);
                    }
                });

        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.second_slide_background)//irst_slide_background
                        .buttonsColor(R.color.second_slide_buttons)
                        //      .image(R.drawable.img_office)
                        .title("Device Health Setup")
                        .description("lets take a few minutes to sign up")
                        .build()
//                ,
//                new MessageButtonBehaviour(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        showMessage("We provide solutions to make you love your work");
//                    }
//                }, "Work with love")
        );
        registerUserFragment = new RegisterUserFragment();
        addSlide(registerUserFragment);


        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.fourth_slide_background)
                .buttonsColor(R.color.fourth_slide_buttons)
                .title("That's it!")
                .description("Enable device health autostart in the autostart manager and reboot device\n\nDevice health is ready for use!")
                .build());

    }

    @Override
    public void onFinish() {
        super.onFinish();
        Toast.makeText(this, "Ill be running in the background! :)", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //  intent.putExtra("forceUpdate", forceUpdate);
        this.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Crashlytics.log(Log.DEBUG, TAG, "onResume ");
        AppController.getInstance().appOpen(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppController.getInstance().appOpen(false);
    }

    /**
     * Handles the Save registration button.
     */
    @Override
    public void onDoRegister(View view) {
        registerUserFragment.onDoRegister();
    }

    /**
     * Handles the balance button.
     */
    @Override
    public void onDoBalance(View view) {
        registerUserFragment.onDoBalance();
    }


}
