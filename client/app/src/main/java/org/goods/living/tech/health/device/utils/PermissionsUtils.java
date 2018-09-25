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
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import org.goods.living.tech.health.device.UI.PermissionActivity;

import java.util.Arrays;
import java.util.List;

/**
 * Utility methods used in this sample.
 */
public class PermissionsUtils {

    final static String TAG = PermissionsUtils.class.getSimpleName();//BaseService.class.getSimpleName();


    public static boolean isLocationOn(Context context) {

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        //Start your Activity if location was enabled:
        return (isGpsEnabled || isNetworkEnabled);


    }

    public static boolean isPermissionGranted(Context context, String perm) {
        if (Build.VERSION.SDK_INT >= 23) {
            //    if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            if (context.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED) {
                Crashlytics.log(Log.DEBUG, TAG, "Permission is granted " + perm);
                return true;
            } else {

                Crashlytics.log(Log.DEBUG, TAG, "Permission is revoked " + perm);
                //   ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Crashlytics.log(Log.DEBUG, TAG, "Permission is granted " + perm);
            return true;
        }
    }


    public static boolean checkAllPermissionsGrantedAndRequestIfNot(Context context) {
        if (!PermissionsUtils.areAllPermissionsGranted(context)) {
            if (!(context instanceof PermissionActivity)) {
                Intent intent = new Intent(context, PermissionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);// | Intent.FLAG_ACTIVITY_NO_HISTORY
                //  intent.putExtra("forceUpdate", forceUpdate);
                context.startActivity(intent);
            }

            return false;
        }
        return true;
    }

    public static boolean areAllSettingPermissionsGranted(Context context) {
        //boolean enabled = USSDService.isAccessibilityServiceEnabled(context);

        boolean enabled = isLocationOn(context);

        return enabled;
    }


    public static boolean areAllPermissionsGranted(Context c) {


        List<String> perms = Arrays.asList(getRequiredPermissions());
        for (String perm : perms) {
            if (!isPermissionGranted(c, perm)) {
                Answers.getInstance().logCustom(new CustomEvent("Missing Permissions")
                        .putCustomAttribute("Reason", perm));
                return false;
            }
        }
        return true;
    }

    public static String[] getRequiredPermissions() {

        return new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                //  Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                //   Manifest.permission.PACKAGE_USAGE_STATS,
                // Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.GET_TASKS,
                //  Manifest.permission.CALL_PRIVILEGED,

        };
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    static boolean hasUsageStatsPermission(Context context) {
        try {
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                    android.os.Process.myUid(), context.getPackageName());
            boolean granted = mode == AppOpsManager.MODE_ALLOWED;
            return granted;
        } catch (Exception e) {
            Crashlytics.logException(e);
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    static boolean hasWriteSettingsPermission(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                boolean write = Settings.System.canWrite(context);
                return write;
            }
            return true;
        } catch (Exception e) {
            Crashlytics.logException(e);
            return false;
        }

    }

    public static void requestBatteryOptimisation(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                String packageName = context.getPackageName();
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    context.startActivity(intent);
                }
            }
        } catch (Exception e) {
            Crashlytics.logException(e);

        }

    }

}
