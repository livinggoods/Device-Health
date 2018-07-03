package org.goods.living.tech.health.device.services;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.firebase.jobdispatcher.JobParameters;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.models.User;
import org.goods.living.tech.health.device.utils.DataBalanceHelper;
import org.goods.living.tech.health.device.utils.PermissionsUtils;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class USSDJobService extends com.firebase.jobdispatcher.JobService {

    final String TAG = this.getClass().getSimpleName();

    public static int runEverySeconds = (int) TimeUnit.HOURS.toSeconds(24); // Every x hours periodicity expressed as seconds


    @Inject
    UserService userService;

    @Inject
    DataBalanceService dataBalanceService;

    @Inject
    DataBalanceHelper dataBalanceHelper;

    @Inject
    RegistrationService registrationService;

    @Override
    public boolean onStartJob(final JobParameters job) {
        // Do some work here

        AppController.getInstance().getComponent().inject(this);

        Crashlytics.log(Log.DEBUG, TAG, "USSDJobService start ...");

        final Context c = this;
        //Offloading work to a new thread.
        new Thread(new Runnable() {
            @Override
            public void run() {
                Crashlytics.log(Log.DEBUG, TAG, "USSDJobService thread ...");

                if (PermissionsUtils.checkAllPermissionsGrantedAndRequestIfNot(c)) {
                    User user = userService.getRegisteredUser();

                    Answers.getInstance().logCustom(new CustomEvent("USSD Job service")
                            .putCustomAttribute("Reason", ""));

                    registrationService.checkBalanceThroughUSSD(c);


//                    dataBalanceHelper.dialNumber(c, ussdFull, new DataBalanceHelper.USSDResult() {
//                        @Override
//                        public void onResult(@NonNull List<DataBalanceHelper.Balance> list) {
//
//                            Crashlytics.log(Log.DEBUG, TAG, "saving balance ...");
//                            for (DataBalanceHelper.Balance b : list) {
//                                dataBalanceService.insert(b.balance, b.rawBalance);
//                            }
//                            ;
//
//                        }
//                    });


                    jobFinished(job, false);
                } else {
                    // ActivityCompat.requestPermissions(c, new String[]{android.Manifest.permission.CALL_PHONE}, 1);
                    Crashlytics.log(Log.DEBUG, TAG, "USSDService permissions not granted yet ...");
                }

            }
        }).start();


        //Tell the framework that the job has completed and does not needs to be reschedule
        //   jobFinished(parameters, true);
        return false; // Answers the question: "Is there still work going on?"
    }


    @Override
    public boolean onStopJob(JobParameters job) {

        Crashlytics.log(Log.DEBUG, TAG, "LocationJobService stop ...");
        return false; // Answers the question: "Should this job be retried?"
    }


}