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


import android.Manifest;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.goods.living.tech.health.device.UI.PermissionActivity;
import org.goods.living.tech.health.device.models.User;
import org.goods.living.tech.health.device.services.UserService;

/**
 * Utility methods used in this sample.
 */
public class PermissionsUtils {

    final static String TAG = PermissionsUtils.class.getSimpleName();//BaseService.class.getSimpleName();
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL = 60; // Every 60 seconds.

    private static final long MAX_WAIT_RECORDS = 2; // Every 5 items

    static FusedLocationProviderClient mFusedLocationClient;

    public static boolean isLocationOn(Context context) {

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        //Start your Activity if location was enabled:
        return (isGpsEnabled || isNetworkEnabled);


    }

    /**
     * Return the current state of the permissions needed.
     */
    public static boolean checkPermissions(Context context) {
        int permissionState = ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    public static void checkAndRequestPermissions(final Context context, UserService userService) {

        try {

            //enable reboot receiver
            ComponentName receiver = new ComponentName(context, LocationUpdatesBroadcastReceiver.class);
            PackageManager pm = context.getPackageManager();

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

            // Check if the user revoked runtime permissions.
            if (!checkPermissions(context)) {
                // requestPermissions;
                //fire dialog activity - coz this method is used in both background activity and activity class
                Intent intent = new Intent(context, PermissionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            } else {
                //do nothing since activity will request permissions
                requestLocationUpdatesIfNotRunning(context, userService);
            }

            if (!isLocationOn(context)) {
                //fire dialog activity - coz this method is used in both background activity and activity class
                Intent intent = new Intent(context, PermissionActivity.class);
                context.startActivity(intent);
            }

        } catch (SecurityException e) {

            e.printStackTrace();
        }


    }

    /**
     * request updates if not already setup
     */
    public static void requestLocationUpdatesIfNotRunning(Context context, UserService userService) {
        try {
            if (mFusedLocationClient != null) {
                return;
            }
            long updateInterval = PermissionsUtils.UPDATE_INTERVAL * 1000;
            long fastestUpdateInterval = updateInterval / 2;
            User user = userService.getRegisteredUser();
            if (user != null) {
                updateInterval = user.updateInterval * 1000;
                fastestUpdateInterval = updateInterval / 2;
            }

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

            LocationRequest mLocationRequest = createLocationRequest(updateInterval);

            mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent(context));
        } catch (SecurityException e) {
            Log.wtf(TAG, e);
        }
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
    private static LocationRequest createLocationRequest(long updateInterval) {
        LocationRequest mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        // Note: apps running on "O" devices (regardless of targetSdkVersion) may receive updates
        // less frequently than this interval when the app is no longer in the foreground.

        long fastestUpdateInterval = updateInterval / 2;

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

    private static PendingIntent getPendingIntent(Context context) {
        // Note: for apps targeting API level 25 ("Nougat") or lower, either
        // PendingIntent.getService() or PendingIntent.getBroadcast() may be used when requesting
        // location updates. For apps targeting API level O, only
        // PendingIntent.getBroadcast() should be used. This is due to the limits placed on services
        // started in the background in "O".

        // TODO(developer): uncomment to use PendingIntent.getService().
        //  Intent intent = new Intent(this, LocationUpdatesIntentService.class);
        //  intent.setAction(LocationUpdatesIntentService.ACTION_PROCESS_UPDATES);
        //  return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent = new Intent(context, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static boolean isStoragePermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                //   ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }
}
