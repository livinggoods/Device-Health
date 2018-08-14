package org.goods.living.tech.health.device.services;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.firebase.jobdispatcher.JobParameters;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.models.Setting;
import org.goods.living.tech.health.device.utils.PermissionsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class LocationJobService extends com.firebase.jobdispatcher.JobService {

    final String TAG = this.getClass().getSimpleName();

    public static int runEverySeconds = (int) TimeUnit.MINUTES.toSeconds(5); // Every x hours periodicity expressed as seconds

    String locOffError = "location is off";

    @Inject
    StatsService statsService;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean onStartJob(final JobParameters job) {
        // Do some work here

        AppController.getInstance().getComponent().inject(this);

        Crashlytics.log(Log.DEBUG, TAG, "LocationJobService start ...");
        logger.debug("LocationJobService start ...");

        final Context c = this;
        //Offloading work to a new thread.
        new Thread(new Runnable() {
            @Override
            public void run() {
                Crashlytics.log(Log.DEBUG, TAG, "LocationJobService thread ...");

                AppController appController = (AppController) c.getApplicationContext();

                Answers.getInstance().logCustom(new CustomEvent("Location Job service")
                        .putCustomAttribute("Reason", ""));

                boolean locationOn = PermissionsUtils.isLocationOn(c);

                Crashlytics.log(Log.DEBUG, TAG, "LocationJobService is location on: " + locationOn);
                Setting setting = appController.getSetting();

                if (!locationOn) {


                    if (setting.loglocationOffEvent) {

                        statsService.insertFailedLocationData(locOffError);
                        Crashlytics.log(Log.DEBUG, TAG, locOffError);
                        Answers.getInstance().logCustom(new CustomEvent("Location")
                                .putCustomAttribute("Reason", locOffError));

                        setting.loglocationOffEvent = false;
                        appController.updateSetting(setting);
                    }
                } else {
                    setting.loglocationOffEvent = true;
                    appController.updateSetting(setting);
                }
                appController.requestLocationUpdates(setting.locationUpdateInterval);
                appController.checkAndRequestPerms();

                jobFinished(job, false);
            }
        }).start();


        //Tell the framework that the job has completed and doesnot needs to be reschedule
        //   jobFinished(parameters, true);
        return true; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(JobParameters job) {

        Crashlytics.log(Log.DEBUG, TAG, "LocationJobService stop ...");
        return false; // Answers the question: "Should this job be retried?"
    }


}