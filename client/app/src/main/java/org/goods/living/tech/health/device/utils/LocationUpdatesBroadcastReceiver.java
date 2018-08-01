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

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.location.LocationResult;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.UI.PermissionActivity;
import org.goods.living.tech.health.device.models.Setting;
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

    public static String locerror = "location turned off";


    @Override
    public void onReceive(Context context, Intent intent) {

        AppController appController;
        if (!(context.getApplicationContext() instanceof AppController)) {
            appController = ((AppController) context.getApplicationContext());

        } else {
            appController = AppController.getInstance();

        }
        appController.getComponent().inject(this);

        boolean locationOn = PermissionsUtils.isLocationOn(context);

        String packageName = appController.appChecker.getForegroundApp(context);

        if (packageName != null && !Utils.isSmartHealthApp(packageName)) {

            String log = "Smarthealth not running. location updates. loc on: " + locationOn;
            Crashlytics.log(Log.DEBUG, TAG, log);
            WriteToLogUtil.getInstance().log(log);
            //  return;
        }


        if (intent != null) {


            if (intent.getAction().contains("PROVIDERS_CHANGED")) {
                String log = "Location Providers changed loc:" + locationOn;
                Crashlytics.log(Log.DEBUG, TAG, log);
                WriteToLogUtil.getInstance().log(log);

                if (!locationOn) {
                    appController.checkAndRequestPerms();
                }
            } else if (ACTION_PROCESS_UPDATES.equals(intent.getAction())) {
                LocationResult result = LocationResult.extractResult(intent);

                Setting setting = appController.getSetting();
                if (!locationOn) {


                    if (setting.loglocationOffEvent) {

                        statsService.insertFailedLocationData(locerror);
                        Crashlytics.log(Log.DEBUG, TAG, locerror);
                        Answers.getInstance().logCustom(new CustomEvent("Location")
                                .putCustomAttribute("Reason", locerror));

                        setting.loglocationOffEvent = false;
                        appController.updateSetting(setting);
                    }
                    appController.checkAndRequestPerms();
                } else {
                    setting.loglocationOffEvent = true;
                    appController.updateSetting(setting);
                }

                if (result != null) {
                    List<Location> locations = result.getLocations();

                    //  Utils.sendNotification(context, "received location updates")
                    String log = "received location updates";
                    Crashlytics.log(Log.DEBUG, TAG, log);
                    WriteToLogUtil.getInstance().log(log);

                    //brightness
                    intent = new Intent(context, PermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                    //  intent.putExtra("forceUpdate", forceUpdate);
                    context.startActivity(intent);

                    Utils.getHandlerThread().post(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Setting setting = appController.getSetting();
                            Integer batteryLevel = Utils.getBatteryPercentage(context);
                            statsService.insertFilteredLocationData(locations, setting.brightness, batteryLevel);

                        }
                    });

                    appController.setUSSDAlarm();
                } else {

                    Crashlytics.log(Log.DEBUG, TAG, "received NULL LocationResult.extractResult(intent). is location on: " + locationOn);


                }
            } else {// if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(intent.getAction())) {

                String log = "Reboot occured - relaunching listeners";
                Crashlytics.log(Log.DEBUG, TAG, log);
                WriteToLogUtil.getInstance().log(log);


                Answers.getInstance().logCustom(new CustomEvent("Reboot")
                        .putCustomAttribute("Reason", ""));

                appController.checkAndRequestPerms();
                appController.setUSSDAlarm();

            }
        }

    }

}


