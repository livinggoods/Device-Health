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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.services.StatsService;
import org.goods.living.tech.health.device.utils.Utils;

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
public class UnlockBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = UnlockBroadcastReceiver.class.getSimpleName();


    @Inject
    StatsService statsService;


    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            AppController appController;
            if (!(context.getApplicationContext() instanceof AppController)) {
                appController = ((AppController) context.getApplicationContext());

            } else {
                appController = AppController.getInstance();

            }
            appController.getComponent().inject(this);
            Crashlytics.log(Log.DEBUG, TAG, "UnlockBroadcastReceiver");


            //  Answers.getInstance().logCustom(new CustomEvent("UnlockBroadcastReceiver")
            //         .putCustomAttribute("Reason", ""));
            Utils.getHandlerThread().post(new Runnable() {
                @Override
                public void run() {
                    try {

                        Location location = appController.getLastLocation();
                        if (location == null) {

                            Answers.getInstance().logCustom(new CustomEvent("UnlockBroadcastReceiver").putCustomAttribute("Reason", "no location"));
                            return;
                        } else {
                            statsService.insertLocation(location, null, null);
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "", e);
                        Crashlytics.logException(e);
                        Utils.dismissProgressDialog();
                    }

                }
            });


        } catch (Exception e) {
            Crashlytics.logException(e);

        }
    }


}


