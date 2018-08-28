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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import org.goods.living.tech.health.device.UI.PermissionActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility methods used in this sample.
 */
public class PermissionsUtils {

    final static String TAG = PermissionsUtils.class.getSimpleName();//BaseService.class.getSimpleName();


    static AlertDialog alertDialog;

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
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
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

    public static boolean checkAllSettingPermissionsGrantedAndRequestIfNot(Context context) {

        if (!areAllSettingPermissionsGranted(context)) {
            if (!(context instanceof PermissionActivity)) {
                Intent intent = new Intent(context, PermissionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                //  intent.putExtra("forceUpdate", forceUpdate);
                context.startActivity(intent);
            }
            return false;
        }
        return true;
    }

    public static boolean checkAllSettingPermissionsGrantedAndDialogRequestIfNot(final Context context) {

        try {
            //TODO: temp disable
//            boolean enabled = USSDService.isAccessibilityServiceEnabled(context);
//            Crashlytics.log(Log.DEBUG, TAG, "isAccessibilityServiceEnabled " + enabled);
//
//            if (!enabled) {
//                //   if (!(context instanceof PermissionActivity))
//                Answers.getInstance().logCustom(new CustomEvent("Missing Permissions")
//                        .putCustomAttribute("Reason", "accessibility"));
//                requestSettingPermissionsWithDialog(context, android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS, "Accessibility", "Reboot after its enabled");
//                return false;
//            }


            if (!isLocationOn(context)) {
                //   if (!(context instanceof PermissionActivity))
                Answers.getInstance().logCustom(new CustomEvent("Missing Permissions")
                        .putCustomAttribute("Reason", "location"));
                requestSettingPermissionsWithDialog(context, android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS, "Location", null);
                requestSettingPermissionsWithDialog(context, android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS, "Location", null);
                return false;
            }


//            if (!hasUsageStatsPermission(context)) {
//                Answers.getInstance().logCustom(new CustomEvent("Missing Permissions")
//                        .putCustomAttribute("Reason", "usage stats"));
//                //   if (!(context instanceof PermissionActivity))
//                requestSettingPermissionsWithDialog(context, Settings.ACTION_USAGE_ACCESS_SETTINGS, "Usage Access", null);
//
//                return false;
//            }

//            if (!hasWriteSettingsPermission(context)) {
//                Answers.getInstance().logCustom(new CustomEvent("Missing Permissions")
//                        .putCustomAttribute("Reason", "write settings"));
//                //   if (!(context instanceof PermissionActivity))
//                requestSettingPermissionsWithDialog(context, Settings.ACTION_MANAGE_WRITE_SETTINGS, "Write Settings", null);
//
//                return false;
//            }


            ;


        } catch (Exception e) {
            Log.wtf(TAG, e);
            Crashlytics.logException(e);
        }

        return true;
    }

    public static void requestSettingPermissionsWithDialog(final Context context, final String permission, String title, String desc) {
        dismissAlert();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title + " Permission");
        builder.setMessage("Please grant this app this permission. " + (desc == null ? "" : desc));//The app needs permissions.
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                Intent intent = new Intent(permission);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);


            }
        });
        // builder.setNegativeButton(android.R.string.no, null);
        builder.setCancelable(false);
        builder.setNegativeButton(null, null);
        alertDialog = builder.show();
    }

    public static void dismissAlert() {
        try {
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
        } catch (Exception e) {
            Crashlytics.logException(e);

        }
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
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                //   Manifest.permission.PACKAGE_USAGE_STATS,
                // Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.GET_TASKS,
                Manifest.permission.READ_PHONE_STATE,
                //  Manifest.permission.CALL_PRIVILEGED,

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
