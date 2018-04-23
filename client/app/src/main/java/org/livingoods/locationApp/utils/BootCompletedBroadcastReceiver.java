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

package org.livingoods.locationApp.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.livingoods.locationApp.UI.MainActivity;
import org.livingoods.locationApp.services.StatsService;

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
public class BootCompletedBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION_PROCESS_UPDATES =
            "org.livingoods.locationApp.locationupdatespendingintent.action" +
                    ".PROCESS_UPDATES";
    private static final String TAG = "LUBroadcastReceiver";

    @Inject
    StatsService statsService;

    @Override
    public void onReceive(Context context, Intent intent) {

        //AppController.getInstance().getComponent().inject(this);

        Log.w("BootCompletedBroadcastReceiver", "starting main...");
        context.startService(new Intent(context, MainActivity.class));
    }
}
