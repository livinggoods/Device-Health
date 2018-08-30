package org.goods.living.tech.health.device.services;

public class LocationJobService {//extends com.firebase.jobdispatcher.JobService {

//    final String TAG = this.getClass().getSimpleName();
//
//    public static int runEverySeconds = (int) TimeUnit.MINUTES.toSeconds(5); // Every x hours periodicity expressed as seconds
//
//    String locOffError = "location is off";
//
//    @Inject
//    StatsService statsService;
//
//    Logger logger = LoggerFactory.getLogger(this.getClass());
//
//    @Override
//    public boolean onStartJob(final JobParameters job) {
//        // Do some work here
//
//        AppController.getInstance().getComponent().inject(this);
//
//        Crashlytics.log(Log.DEBUG, TAG, "LocationJobService start ...");
//        logger.debug("LocationJobService start ...");
//
//        final Context c = this;
//        //Offloading work to a new thread.
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Crashlytics.log(Log.DEBUG, TAG, "LocationJobService thread ...");
//
//                AppController appController = (AppController) c.getApplicationContext();
//
//                Answers.getInstance().logCustom(new CustomEvent("Location Job service")
//                        .putCustomAttribute("Reason", ""));
//
//                Utils.turnGPSOn(c);
//                boolean locationOn = PermissionsUtils.isLocationOn(c);
//
//                Crashlytics.log(Log.DEBUG, TAG, "LocationJobService is location on: " + locationOn);
//                Setting setting = appController.getSetting();
//
//                if (!locationOn) {
//
//
//                    if (setting.loglocationOffEvent) {
//
//                        statsService.insertMessageData(locOffError);
//                        Crashlytics.log(Log.DEBUG, TAG, locOffError);
//                        Answers.getInstance().logCustom(new CustomEvent("Location")
//                                .putCustomAttribute("Reason", locOffError));
//
//                        setting.loglocationOffEvent = false;
//                        appController.updateSetting(setting);
//                    }
//                    appController.checkAndRequestPerms();
//
//                } else {
//                    setting.loglocationOffEvent = true;
//                    appController.updateSetting(setting);
//                }
//
//                appController.requestLocationUpdates(setting.locationUpdateInterval * 1000);
//
//
//                jobFinished(job, false);
//            }
//        }).start();
//
//
//        //Tell the framework that the job has completed and doesnot needs to be reschedule
//        //   jobFinished(parameters, true);
//        return true; // Answers the question: "Is there still work going on?"
//    }
//
//    @Override
//    public boolean onStopJob(JobParameters job) {
//
//        Crashlytics.log(Log.DEBUG, TAG, "LocationJobService stop ...");
//        return false; // Answers the question: "Should this job be retried?"
//    }


}