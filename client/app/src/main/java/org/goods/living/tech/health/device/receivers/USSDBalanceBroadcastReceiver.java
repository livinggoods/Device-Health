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

package org.goods.living.tech.health.device.receivers;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.models.Setting;
import org.goods.living.tech.health.device.services.DataBalanceService;
import org.goods.living.tech.health.device.services.RegistrationService;
import org.goods.living.tech.health.device.services.UserService;
import org.goods.living.tech.health.device.utils.DataBalanceHelper;

import java.util.Calendar;

import javax.inject.Inject;

/**
 * Receiver for handling location updates.
 * <p>
 * For apps targeting API level O
 * {@link android.app.PendingIntent#getBroadcast(Context, int, Intent, int)} should be used when
 * requesting location updates. Due to limits on background services,
 * {@link android.app.PendingIntent#getService(Context, int, Intent, int)} should not be used.
 * <p>
 * Note: Apps running on "O" devices (regardless of targetSdkVersion) may receive updates
 * less frequently than the interval specified in the
 * {@link com.google.android.gms.location.LocationRequest} when the app is no longer in the
 * foreground.
 */
public class USSDBalanceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = USSDBalanceBroadcastReceiver.class.getSimpleName();

    @Inject
    UserService userService;

    @Inject
    DataBalanceService dataBalanceService;

    @Inject
    RegistrationService registrationService;

    // idle mode
    //adb shell dumpsys deviceidle force-idle   adb shell dumpsys deviceidle unforce
    //reactivate: adb shell dumpsys battery reset
    //standby  adb shell dumpsys battery unplug
    //$ adb shell am set-inactive <packageName> true
    //wake: adb shell am set-inactive <packageName> false
    //$ adb shell am get-inactive <packageName>

    @Override
    public void onReceive(Context context, Intent intent) {

        AppController appController;
        if (!(context.getApplicationContext() instanceof AppController)) {
            appController = ((AppController) context.getApplicationContext());

        } else {
            appController = AppController.getInstance();

        }
        appController.getComponent().inject(this);
        Crashlytics.log(Log.DEBUG, TAG, "USSDBalanceBroadcastReceiver");


        // unlock(context);
        //Offloading work to a new thread.
        new Thread(new Runnable() {
            @Override
            public void run() {
                Crashlytics.log(Log.DEBUG, TAG, "USSDJobService thread ...");

                Setting setting = AppController.getInstance().getSetting();
                setting.lastUSSDRun = Calendar.getInstance().getTime();
                AppController.getInstance().updateSetting(setting);


                // if (PermissionsUtils.checkAllPermissionsGrantedAndRequestIfNot(appController.getApplicationContext())) {
                // User user = userService.getRegisteredUser();

                Answers.getInstance().logCustom(new CustomEvent("USSD Job service")
                        .putCustomAttribute("Reason", ""));

                //  registrationService.checkBalanceThroughUSSD(c);
                //   if (setting.lastUSSDRun == null || setting.lastUSSDRun.before(yesterday.getTime())) {
                registrationService.checkBalanceThroughSMS(appController.getApplicationContext(), null);

                //    }


            }
        }).start();


    }

    void unlock(Context context) {
        try {
            KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock");
            kl.disableKeyguard();

            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                    | PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
            wakeLock.acquire(DataBalanceHelper.USSD_LIMIT * 1000);


        } catch (Exception e) {
            // Log.e(TAG, e.toString());
            Crashlytics.logException(e);

        }
    }
}


