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
package org.goods.living.tech.health.device.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.utils.Utils;

import java.util.List;

import javax.inject.Inject;

/**
 * Handles incoming location updates and displays a notification with the location data.
 * <p>
 * For apps targeting API level 25 ("Nougat") or lower, location updates may be requested
 * using {@link android.app.PendingIntent#getService(Context, int, Intent, int)} or
 * {@link android.app.PendingIntent#getBroadcast(Context, int, Intent, int)}. For apps targeting
 * API level O, only {@code getBroadcast} should be used.
 * <p>
 * Note: Apps running on "O" devices (regardless of targetSdkVersion) may receive updates
 * less frequently than the interval specified in the
 * {@link com.google.android.gms.location.LocationRequest} when the app is no longer in the
 * foreground.
 */
public class LocationUpdatesIntentService { //extends IntentService {

   /* public static final String ACTION_PROCESS_UPDATES =
            "org.goods.living.tech.health.device.locationupdatespendingintent.action" +
                    ".PROCESS_UPDATES";
    private static final String TAG = LocationUpdatesIntentService.class.getSimpleName();

    @Inject
    StatsService statsService;

    public LocationUpdatesIntentService() {
        // Name the worker thread.
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        AppController.getInstance().getComponent().inject(this);

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {

                    List<Location> locations = result.getLocations();
                    Utils.sendNotification(this, "received location updates");
                    Log.i(TAG, "received location updates");

                    statsService.insertLocationData(locations);
                }else{
                    Log.i(TAG, "received NULL LocationResult.extractResult(intent) location updates ");
                }
            }
        }
    }*/
}
