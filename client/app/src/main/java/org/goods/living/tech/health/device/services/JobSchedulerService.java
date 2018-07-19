package org.goods.living.tech.health.device.services;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;

import org.goods.living.tech.health.device.AppController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobSchedulerService extends com.firebase.jobdispatcher.JobService {

    final String TAG = this.getClass().getSimpleName();

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public static String JOB_NAME = "JOB_NAME";

    @Override
    public boolean onStartJob(final JobParameters job) {
        // Do some work here

        AppController.getInstance().getComponent().inject(this);

        Crashlytics.log(Log.DEBUG, TAG, "JobSchedulerService start ...");
        //  logger.debug("JobSchedulerService start ...");

        final Context c = this;

        String jobToStart = job.getExtras().getString(JOB_NAME);
        //Offloading work to a new thread.
        new Thread(new Runnable() {
            @Override
            public void run() {
                Crashlytics.log(Log.DEBUG, TAG, "JobSchedulerService thread ...");

                AppController appController = (AppController) c.getApplicationContext();

                if (jobToStart.equals(LocationJobService.class.getName())) {
                    AppController.getInstance().dispatcher.mustSchedule(createLocationJob(job.getExtras()));
                } else if (jobToStart.equals(USSDJobService.class.getName())) {
                    AppController.getInstance().dispatcher.mustSchedule(createUSSDJob(job.getExtras()));
                }


                jobFinished(job, false);
            }
        }).start();


        //Tell the framework that the job has completed and doesnot needs to be reschedule
        //   jobFinished(parameters, true);
        return false; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(JobParameters job) {

        Crashlytics.log(Log.DEBUG, TAG, "JobSchedulerService stop ...");
        return false; // Answers the question: "Should this job be retried?"
    }

    Job createLocationJob(Bundle myExtrasBundle) {

        Job job = AppController.getInstance().createJob(LocationJobService.class, LocationJobService.class.getName(), LocationJobService.runEverySeconds, true, myExtrasBundle);

        return job;
    }

    Job createUSSDJob(Bundle myExtrasBundle) {

        Job job = AppController.getInstance().createJob(USSDJobService.class, USSDJobService.class.getName(), USSDJobService.runEverySeconds, true, myExtrasBundle);

        return job;

    }

}