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
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.goods.living.tech.health.device.AppController;

import java.util.ArrayList;
import java.util.List;

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
public class ActivityUpdatesService extends IntentService {


    private static final String TAG = ActivityUpdatesService.class.getName();

    int[] act = getActivities();
    List<Integer> monitoredActivities;

    int THRESHHOLD_CONFIDENCE = 50;

    {
        monitoredActivities = new ArrayList<>(act.length);
        for (int i : act) {
            monitoredActivities.add(i);
        }

    }

    public ActivityUpdatesService() {
        // Name the worker thread.
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        AppController appController;
        if (!(this.getApplicationContext() instanceof AppController)) {
            appController = ((AppController) this.getApplicationContext());

        } else {
            appController = AppController.getInstance();

        }
        appController.getComponent().inject(this);

        Crashlytics.log(Log.DEBUG, TAG, "Activity Detected");

        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        // Get the list of the probable activities associated with the current state of the
        // device. Each activity is associated with a confidence level (between 0-100)

        List<DetectedActivity> detectedActivities = result.getProbableActivities();

        boolean movement = false;
        for (DetectedActivity activity : detectedActivities) {//hoping they r ordered by priority
            Crashlytics.log(Log.DEBUG, TAG, "Detected activity: " + activityName(activity.getType()) + " " + activity.getType() + ", " + activity.getConfidence());
            //broadcastActivity(activity);
            // DetectedActivity.IN_VEHICLE;

            if (activity.getConfidence() > THRESHHOLD_CONFIDENCE && monitoredActivities.contains(activity.getType())) {//hoping they r ordered by priority
                Crashlytics.log(Log.DEBUG, TAG, "Detected priority activity: " + activityName(detectedActivities.get(0).getType()) + " " + detectedActivities.get(0).getType() + ", " + detectedActivities.get(0).getConfidence());
                movement = true;//break to ignore loop?
            }

        }


        if (movement) {
            Crashlytics.log(Log.DEBUG, TAG, "Launching Location listener");

            appController.requestLocationUpdates();
        }


    }

    String activityName(int type) {
        String label = "Unknown";
        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                label = "In_Vehicle";
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                label = "On_Bicycle";
                break;
            }
            case DetectedActivity.ON_FOOT: {
                label = "On_Foot";
                break;
            }
            case DetectedActivity.RUNNING: {
                label = "Running";
                break;
            }
            case DetectedActivity.STILL: {
                label = "Still";
                break;
            }
            case DetectedActivity.TILTING: {
                label = "Tilting";
                break;
            }
            case DetectedActivity.WALKING: {
                label = "Walking";
                break;
            }
            case DetectedActivity.UNKNOWN: {
                break;
            }


        }
        return label;
    }

    public static int[] getActivities() {

        return new int[]{DetectedActivity.IN_VEHICLE,
                DetectedActivity.ON_BICYCLE,
                DetectedActivity.ON_FOOT,
                DetectedActivity.RUNNING,
                DetectedActivity.WALKING,

        };
    }
}


