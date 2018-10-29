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

package org.goods.living.tech.health.device.receivers;

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
import org.goods.living.tech.health.device.models.Stats;
import org.goods.living.tech.health.device.services.StatsService;
import org.goods.living.tech.health.device.utils.PermissionsUtils;
import org.goods.living.tech.health.device.utils.Utils;
import org.goods.living.tech.health.device.utils.WriteToLogUtil;

import java.util.ArrayList;
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

//https://developer.android.com/guide/topics/location/battery#understand
public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_PROCESS_UPDATES =
            ".org.goods.living.tech.health.device.LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES";
    private static final String TAG = LocationUpdatesBroadcastReceiver.class.getName();

    @Inject
    StatsService statsService;


    public static String locerror = "location turned off";

    /**
     * Time difference threshold
     */
    static final int TIME_DIFFERENCE_THRESHOLD = 3 * 60 * 1000;


    @Override
    public void onReceive(Context context, Intent intent) {

        try {

            //appController = ((AppController) context.getApplicationContext());

            AppController.getInstance().getComponent().inject(this);

            boolean locationOn = PermissionsUtils.isLocationOn(context);

            //  location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            String packageName = AppController.getInstance().appChecker.getForegroundApp(context);

            if (packageName != null && Utils.isSmartHealthApp(packageName)) {

                String log = "Smarthealth running. location updates. loc on: " + locationOn;
                Crashlytics.log(Log.DEBUG, TAG, log);
                WriteToLogUtil.getInstance().log(log);
                statsService.insertMessageData(log);
                //  return;
            }


            if (intent != null) {


                if (intent.getAction().contains("PROVIDERS_CHANGED")) {
                    String log = "Location Providers changed loc:" + locationOn;
                    Crashlytics.log(Log.DEBUG, TAG, log);
                    WriteToLogUtil.getInstance().log(log);

                    Setting setting = AppController.getInstance().getSetting();
                    if (!locationOn) {


                        if (setting.loglocationOffEvent) {

                            statsService.insertMessageData(locerror);
                            Crashlytics.log(Log.DEBUG, TAG, locerror);
                            Answers.getInstance().logCustom(new CustomEvent("Location")
                                    .putCustomAttribute("Reason", locerror));

                            setting.loglocationOffEvent = false;
                            AppController.getInstance().updateSetting(setting);
                        }

                    } else {
                        setting.loglocationOffEvent = true;
                        AppController.getInstance().updateSetting(setting);
                    }

                    if (!locationOn) {
                        AppController.getInstance().checkAndRequestPerms();
                    }


                } else if (ACTION_PROCESS_UPDATES.equals(intent.getAction())) {
                    LocationResult result = LocationResult.extractResult(intent);
                    if (result != null) {
                        processLocation(context, result.getLocations());

                    }
                } else {// if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(intent.getAction())) {

                    String log = "Reboot occured - relaunching listeners";
                    Crashlytics.log(Log.DEBUG, TAG, log);
                    WriteToLogUtil.getInstance().log(log);


                    Answers.getInstance().logCustom(new CustomEvent("Reboot")
                            .putCustomAttribute("Reason", ""));

                    Setting setting = AppController.getInstance().getSetting();

                    AppController.getInstance().checkAndRequestPerms();
                    AppController.getInstance().setUSSDAlarm(setting.disableDatabalanceCheck, setting.getDatabalanceCheckTimeInMilli());

                    AppController.getInstance().requestLocationUpdates();


                }
            }
        } catch (Exception e) {
            Crashlytics.logException(e);

        }
    }

    public static Location locationFromStats(Stats stat) {

        if (stat == null) {
            return null;
        }

        Location loc = new Location(stat.provider);
        loc.setTime(stat.recordedAt.getTime());
        loc.setLatitude(stat.latitude);
        loc.setLongitude(stat.longitude);
        loc.setAccuracy((float) stat.accuracy);

        return loc;

    }

    void processLocation(Context context, List<Location> locations) {
        {
            List<Location> filteredLocs = new ArrayList<Location>();

            List<Stats> l = statsService.getLatestRecords(1l);

            Location oldLocation = l.size() > 0 ? locationFromStats(l.get(0)) : null;


            for (Location location : locations) {

                if (isBetterLocation(oldLocation, location)) {
                    // If location is better.
                    Crashlytics.log(Log.DEBUG, TAG, "better location found");
                    //remove duplicates
                    if (oldLocation != null && oldLocation.getLatitude() == location.getLatitude() && oldLocation.getLongitude() == location.getLongitude()) {
                        Crashlytics.log(Log.DEBUG, TAG, "same location found - skipping");
                        continue;
                    }

                    oldLocation = location;

                    filteredLocs.add(location);
                }

            }
            if (filteredLocs.size() < 1) {
                Crashlytics.log(Log.DEBUG, TAG, "all locations filtered out");
                return;
            }


            //  Utils.sendNotification(context, "received location updates")
            String log = "received location updates";
            Crashlytics.log(Log.DEBUG, TAG, log);
            WriteToLogUtil.getInstance().log(log);

            if (AppController.inBackground) {

                Crashlytics.log(Log.DEBUG, TAG, "isAppOpen");

                //brightness
                Intent intent = new Intent(context, PermissionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//| Intent.FLAG_ACTIVITY_NO_HISTORY
                //  intent.putExtra("forceUpdate", forceUpdate);
                context.startActivity(intent);
                //  return;
            }
            Setting setting = AppController.getInstance().getSetting();
            Utils.getHandlerThread().post(new Runnable() {
                @Override
                public void run() {

                    try {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Integer batteryLevel = Utils.getBatteryPercentage(context);
                        statsService.insertLocationData(filteredLocs, setting.brightness, batteryLevel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });


        }
    }

    public static Location getBestLastLocation(Location oldLocation, Location newLocation) {

        Location loc = isBetterLocation(oldLocation, newLocation) ? newLocation : oldLocation;
        oldLocation = loc;
        return oldLocation;
    }

    static boolean isBetterLocation(Location oldLocation, Location newLocation) {
        // If there is no old location, of course the new location is better.
        if (oldLocation == null) {
            return true;
        }
        if (newLocation == null) {
            return false;
        }
        // Check if new location is newer in time.
        boolean isNewer = newLocation.getTime() > oldLocation.getTime();

        // Check if new location more accurate. Accuracy is radius in meters, so less is better.
        boolean isMoreAccurate = newLocation.getAccuracy() <= oldLocation.getAccuracy();
        if (isMoreAccurate && isNewer) {
            // More accurate and newer is always better.
            return true;
        } else {// if (isMoreAccurate && !isNewer) {
            // More accurate but not newer can lead to bad fix because of user movement.
            // Let us set a threshold for the maximum tolerance of time difference.
            long timeDifference = newLocation.getTime() - oldLocation.getTime();

            // If time difference is not greater then allowed threshold we accept it.
            if (timeDifference > TIME_DIFFERENCE_THRESHOLD) {
                return true;
            }
        }

        return false;
    }


}


