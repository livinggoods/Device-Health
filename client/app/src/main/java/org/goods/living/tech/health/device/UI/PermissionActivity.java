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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.BuildConfig;
import org.goods.living.tech.health.device.R;
import org.goods.living.tech.health.device.models.Setting;
import org.goods.living.tech.health.device.models.User;
import org.goods.living.tech.health.device.utils.PermissionsUtils;
import org.goods.living.tech.health.device.utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PermissionActivity extends FragmentActivity {


    private static final String TAG = PermissionActivity.class.getSimpleName();

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;


    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    // private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 5; // Every 5 minutes.
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.Theme_Transparent);
        super.onCreate(savedInstanceState);

        AppController.getInstance().getComponent().inject(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        checkPerms();
    }

    void checkPerms() {

        PermissionsUtils.dismissAlert();

        Float bright = Utils.getBrightness(this, getWindow());

        Setting setting = AppController.getInstance().getSetting();
        setting.brightness = bright != null ? bright.doubleValue() : null;
        AppController.getInstance().updateSetting(setting);
        if (!PermissionsUtils.areAllPermissionsGranted(this)) {

            //request permissions
            PermissionsUtils.requestAllPermissions(this);
        } else {
            if (!PermissionsUtils.checkAllSettingPermissionsGrantedAndDialogRequestIfNot(this)) {

            } else {

                minimise();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

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


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Crashlytics.log(Log.DEBUG, TAG, "onRequestPermissionResult");


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


                    Snackbar.make(
                            getWindow().getDecorView().getRootView(),//findViewById(R.id.activity_main),
                            getString(R.string.permission_denied_explanation, name),
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.settings, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Build intent that displays the App settings screen.
                                    Intent intent = new Intent();
                                    intent.setAction(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package",
                                            BuildConfig.APPLICATION_ID, null);
                                    intent.setData(uri);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    minimise();
                                }
                            })
                            .show();
                    return;
                } else {
                    //perm granted
                    Crashlytics.log(Log.DEBUG, TAG, "Permission was granted " + permissions[i]);

                    if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)
                            || permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {

                        User user = AppController.getInstance().getUser();
                        AppController appController = (AppController) this.getApplicationContext();
                        appController.requestLocationUpdates(user.updateInterval);
                    }
                }
            }
        }
        checkPerms();
    }


    public void minimise() {

        //  Intent intent = new Intent(this, MainActivity.class);
        //  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //  startActivity(intent);
        // moveTaskToBack(true);
        finish();
    }
}