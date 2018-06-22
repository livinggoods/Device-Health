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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.goods.living.tech.health.device.UI.PermissionActivity;
import org.goods.living.tech.health.device.models.User;
import org.goods.living.tech.health.device.services.USSDService;
import org.goods.living.tech.health.device.services.UserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility methods used in this sample.
 */
public class PermissionsUtils {

    final static String TAG = PermissionsUtils.class.getSimpleName();//BaseService.class.getSimpleName();
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL = 300; // seconds.

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
     * request updates if not already setup
     */
    public static void requestLocationUpdates(Context context, UserService userService, boolean forceUpdate) {
        try {

            long updateInterval = PermissionsUtils.UPDATE_INTERVAL * 1000;

            if (mFusedLocationClient == null) {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);


                User user = userService.getRegisteredUser();
                if (user != null) {
                    updateInterval = user.updateInterval * 1000;
                }


            } else {

                if (forceUpdate) {
                    LocationRequest mLocationRequest = createLocationRequest(updateInterval);

                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent(context));
                }
            }


        } catch (SecurityException e) {
            Log.wtf(TAG, e);
            Crashlytics.logException(e);
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

    static boolean isPermissionGranted(Context context, String perm) {
        if (Build.VERSION.SDK_INT >= 23) {
            //    if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            if (context.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED) {
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


    public static boolean checkAllPermissionsGrantedAndRequestIfNot(Context context) {
        if (!PermissionsUtils.areAllPermissionsGranted(context)) {
            Intent intent = new Intent(context, PermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //  intent.putExtra("forceUpdate", forceUpdate);
            context.startActivity(intent);
            return false;
        }
        return true;
    }

    public static boolean areAllSettingPermissionsGranted(Context context) {
        boolean enabled = USSDService.isAccessibilityServiceEnabled(context);

        enabled = enabled && isLocationOn(context);

        return enabled;
    }

    public static boolean checkAllSettingPermissionsGrantedAndRequestIfNot(Context context) {

        if (!areAllSettingPermissionsGranted(context)) {
            Intent intent = new Intent(context, PermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //  intent.putExtra("forceUpdate", forceUpdate);
            context.startActivity(intent);
            return false;
        }
        return true;
    }

    public static boolean checkAllSettingPermissionsGrantedAndDialogRequestIfNot(final Context context) {

        try {
            boolean enabled = USSDService.isAccessibilityServiceEnabled(context);
            Log.i(TAG, "isAccessibilityServiceEnabled " + enabled);

            if (!enabled) {
                requestSettingPermissionsWithDialog(context, android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS, "Accessibility");
                return false;
            }

            if (!isLocationOn(context)) {
                requestSettingPermissionsWithDialog(context, android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS, "Location");
                return false;
            }
        } catch (Exception e) {
            Log.wtf(TAG, e);
            Crashlytics.logException(e);
        }

        return true;
    }

    public static void requestSettingPermissionsWithDialog(final Context context, String permission, String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title + " Permission");
        builder.setMessage("The app needs permissions. Please grant this permission to continue using the features of the app.");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                context.startActivity(intent);
            }
        });
        // builder.setNegativeButton(android.R.string.no, null);
        builder.setCancelable(false);
        builder.setNegativeButton(null, null);
        builder.show();
    }

    public static boolean areAllPermissionsGranted(Context c) {


        List<String> perms = Arrays.asList(getRequiredPermissions());
        for (String perm : perms) {
            if (!isPermissionGranted(c, perm)) {
                return false;
            }
        }
        return true;
    }

    public static String[] getRequiredPermissions() {

        return new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_SMS
        };
    }

    public static void requestAllPermissions(Activity c) {

        Log.v("TAG", "Requesting All non granted Permission");

        List<String> perms = Arrays.asList(getRequiredPermissions());
        List<String> nonGranted = new ArrayList<String>();
        for (String perm : perms) {
            if (!isPermissionGranted(c, perm)) {
                nonGranted.add(perm);
            }
        }
        if (nonGranted.size() > 0) {
            requestPermissions(c, nonGranted.toArray(new String[nonGranted.size()]));
        }

    }

    //new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED}
    static void requestPermissions(Activity context, String[] perms) {

        Log.v("TAG", "Requesting Permission " + perms.toString());
        ActivityCompat.requestPermissions(context, perms, 1);
    }
}
