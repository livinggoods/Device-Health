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

package org.goods.living.tech.health.device.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.UI.MainActivity;
import org.goods.living.tech.health.device.services.StatsService;

import java.util.List;

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
public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_PROCESS_UPDATES =
                    ".org.goods.living.tech.health.device.LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES";
    private static final String TAG = "LUBroadcastReceiver";

    @Inject
    StatsService statsService;

    @Override
    public void onReceive(Context context, Intent intent) {

        AppController.getInstance().getComponent().inject(this);




        if (intent != null) {

            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED"))
            {
                Log.i(TAG, "Location Providers changed");

                boolean on = Utils.isLocationOn(context);

                Log.i(TAG, "Location Providers on: "+ on);
                //Start your Activity if location was enabled:
                if (Utils.isLocationOn(context)) {
                    Intent i = new Intent(context, MainActivity.class);
                    context.startActivity(i);
                }
            }
            else if (ACTION_PROCESS_UPDATES.matches(intent.getAction())) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> locations = result.getLocations();


                    Utils.sendNotification(context, "received location updates");
                    Log.i(TAG, "received location updates");

                    statsService.insertFilteredLocationData(locations);
                }else{
                    Log.i(TAG, "received NULL LocationResult.extractResult(intent) location updates ");
                }
            }
        }
    }
}
