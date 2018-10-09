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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.BuildConfig;
import org.goods.living.tech.health.device.models.Setting;
import org.goods.living.tech.health.device.models.User;
import org.goods.living.tech.health.device.utils.PermissionsUtils;
import org.goods.living.tech.health.device.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PermissionActivity extends FragmentActivity {


    private static final String TAG = PermissionActivity.class.getSimpleName();

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    AlertDialog alertDialog;

    boolean openMain;

    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    // private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 5; // Every 5 minutes.
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //  setTheme(R.style.Theme_Transparent);
        super.onCreate(savedInstanceState);

        AppController.getInstance().getComponent().inject(this);


    }

    @Override
    protected void onStart() {
        super.onStart();

        checkPerms();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();//finishandremovetask
    }

    void checkPerms() {
        try {
            dismissAlert();

            Float bright = Utils.getBrightness(this, getWindow());

            Setting setting = AppController.getInstance().getSetting();
            setting.brightness = bright != null ? bright.doubleValue() : null;
            AppController.getInstance().updateSetting(setting);
            if (!PermissionsUtils.areAllPermissionsGranted(this)) {

                //request permissions
                openMain = true;
                requestAllPermissions(this);

            } else {

                if (!PermissionsUtils.isLocationOn(this)) {
                    //   if (!(context instanceof PermissionActivity))
                    Answers.getInstance().logCustom(new CustomEvent("Missing Permissions")
                            .putCustomAttribute("Reason", "location"));
                    requestSettingPermissionsWithDialog(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS, "Location", null);

                }
                minimise();
            }
        } catch (Exception e) {
            Crashlytics.logException(e);

        }
    }

    @Override
    protected void onDestroy() {
        dismissAlert();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Crashlytics.log(Log.DEBUG, TAG, "onResume ");

    }

//    private void requestLocationPermissions() {
//        boolean shouldProvideRationale =
//                ActivityCompat.shouldShowRequestPermissionRationale(this,
//                        Manifest.permission.ACCESS_FINE_LOCATION);
//
//        // Provide an additional rationale to the user. This would happen if the user denied the
//        // request previously, but didn't check the "Don't ask again" checkbox.
//        if (shouldProvideRationale) {
//             Crashlytics.log(Log.DEBUG, TAG, "Displaying permission rationale to provide additional context.");
//            Snackbar.make(
//                    getWindow().getDecorView().getRootView(),// findViewById(R.id.activity_main),
//                    R.string.permission_rationale,
//                    Snackbar.LENGTH_INDEFINITE)
//                    .setAction("Ok", new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            // Request permission
//                            ActivityCompat.requestPermissions(PermissionActivity.this,
//                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                                    REQUEST_PERMISSIONS_REQUEST_CODE);
//                        }
//                    })
//                    .show();
//        } else {
//             Crashlytics.log(Log.DEBUG, TAG, "Requesting permission");
//            // Request permission. It's possible this can be auto answered if device policy
//            // sets the permission in a given state or the user denied the permission
//            // previously and checked "Never ask again".
//            ActivityCompat.requestPermissions(PermissionActivity.this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    REQUEST_PERMISSIONS_REQUEST_CODE);
//        }
//    }

    //optional
//    @AskGrantedAll//(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//    public void accessGranted(int id) {
//        Log.i(TAG, id + "  GRANTED");
//
//
//    }

//    //optional
//    @AskDenied(AskDenied)
//    public void fileAccessDenied(int id) {
//        Log.i(TAG, id + "  DENiED");
//    }
//

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {


        try {
            //     if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Crashlytics.log(Log.DEBUG, TAG, "User interaction was cancelled.");

            } else {

                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        //  Crashlytics.log(Log.DEBUG, TAG, "Permission denied " + permissions[i]);

                        Crashlytics.log("Permission denied " + permissions[i]);
                        Answers.getInstance().logCustom(new CustomEvent("Permission denied")
                                .putCustomAttribute("Reason", permissions[i]));
                        // Permission denied.

                        // Notify the user via a SnackBar that they have rejected a core permission for the
                        // app, which makes the Activity useless. In a real app, core permissions would
                        // typically be best requested during a welcome-screen flow.

                        // Additionally, it is important to remember that a permission might have been
                        // rejected without asking the user for permission (device policy or "Never ask
                        // again" prompts). Therefore, a user interface affordance is typically implemented
                        // when permissions are denied. Otherwise, your app could appear unresponsive to
                        // touches or interactions which have required permissions.

                        //  Pattern p = Pattern.compile("^.+(\\d+).+");
                        Pattern p = Pattern.compile("[^\\.]*$");
                        Matcher m = p.matcher(permissions[i]);

                        String name = permissions[i];
                        if (m.find()) {
                            name = m.group(0);
                        }

                        Intent intent = new Intent();
                        intent.setAction(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package",
                                BuildConfig.APPLICATION_ID, null);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


                        requestSettingPermissionsWithDialog(intent, name, null);

                        return;
                    } else {
                        //perm granted
                        Crashlytics.log(Log.DEBUG, TAG, "Permission was granted " + permissions[i]);

                        if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {

                            User user = AppController.getInstance().getUser();
                            AppController appController = (AppController) this.getApplicationContext();
                            appController.requestActivityRecognition(appController.getSetting().locationUpdateInterval * 1000);
                        }
                    }
                }
            }

            checkPerms();
        } catch (Exception e) {
            Crashlytics.logException(e);
            minimise();
        }
    }


    public void minimise() {

        try {
            dismissAlert();
            if (openMain == true) {
                openMain = false;
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {

                moveTaskToBack(true);// finish();

            }


        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }


    void requestPermissions(String[] perms) {
        try {
            Log.v("TAG", "Requesting Permission " + perms.toString());
            if (!isFinishing()) {
                //show dialog
                ActivityCompat.requestPermissions(this, perms, 1);

            }


//            Ask.on(this)
//                    .id(1) // in case you are invoking multiple time Ask from same activity or fragment
//                    .forPermissions(perms)
//                    .withRationales("Permissions needed for app to work properly",
//                            "You will need to grant permission") //optional
//                    .go();


        } catch (Exception e) {
            Crashlytics.logException(e);

        }
    }


    void requestAllPermissions(Activity c) {

        Log.v("TAG", "Requesting All non granted Permission");

        List<String> perms = Arrays.asList(PermissionsUtils.getRequiredPermissions());
        List<String> nonGranted = new ArrayList<String>();
        for (String perm : perms) {
            if (!PermissionsUtils.isPermissionGranted(c, perm)) {
                nonGranted.add(perm);
            }
        }
        if (nonGranted.size() > 0) {
            requestPermissions(nonGranted.toArray(new String[nonGranted.size()]));
        }

    }

    void requestSettingPermissionsWithDialog(final String permission, String title, String desc) {
        dismissAlert();
        Context c = this;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title + " Permission");
        builder.setMessage("Please grant this app this permission. " + (desc == null ? "" : desc));//The app needs permissions.
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                Intent intent = new Intent(permission);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                c.startActivity(intent);


            }
        });
        // builder.setNegativeButton(android.R.string.no, null);
        builder.setCancelable(false);
        builder.setNegativeButton(null, null);
        alertDialog = builder.show();
    }

    void requestSettingPermissionsWithDialog(final Intent intent, String title, String desc) {
        dismissAlert();
        Context c = this;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title + " Permission");
        builder.setMessage("Please grant this app this permission. " + (desc == null ? "" : desc));//The app needs permissions.
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                c.startActivity(intent);


            }
        });
        // builder.setNegativeButton(android.R.string.no, null);
        builder.setCancelable(false);
        builder.setNegativeButton(null, null);
        alertDialog = builder.show();
    }

    void dismissAlert() {
        try {
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
        } catch (Exception e) {
            Crashlytics.logException(e);

        }
    }
}