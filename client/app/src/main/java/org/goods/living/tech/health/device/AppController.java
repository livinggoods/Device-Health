package org.goods.living.tech.health.device;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.core.CrashlyticsCore;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rvalerio.fgchecker.AppChecker;

import org.goods.living.tech.health.device.UI.MainActivity;
import org.goods.living.tech.health.device.UI.PermissionActivity;
import org.goods.living.tech.health.device.models.Setting;
import org.goods.living.tech.health.device.models.User;
import org.goods.living.tech.health.device.receivers.LocationUpdatesBroadcastReceiver;
import org.goods.living.tech.health.device.receivers.USSDBalanceBroadcastReceiver;
import org.goods.living.tech.health.device.services.ActivityUpdatesService;
import org.goods.living.tech.health.device.services.SettingService;
import org.goods.living.tech.health.device.services.UserService;
import org.goods.living.tech.health.device.utils.AuthenticatorService;
import org.goods.living.tech.health.device.utils.PermissionsUtils;
import org.goods.living.tech.health.device.utils.SyncAdapter;
import org.goods.living.tech.health.device.utils.TelephonyUtil;
import org.goods.living.tech.health.device.utils.Utils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.fabric.sdk.android.Fabric;

public class AppController extends Application {

    // Create the instance
    private static AppController instance;

    private AppcontrollerComponent component;

    FirebaseAnalytics mFirebaseAnalytics;

    public FirebaseJobDispatcher dispatcher;

    FusedLocationProviderClient mFusedLocationClient;

    ActivityRecognitionClient mActivityRecognitionClient;

    final String TAG = this.getClass().getSimpleName();//BaseService.class.getSimpleName();
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    UserService userService;
    @Inject
    SettingService settingService;

    public TelephonyUtil telephonyInfo;

    public AppChecker appChecker;

    Intent restartServiceIntent;


    Location location;

    public static boolean inBackground = true;

    private static final long MAX_WAIT_RECORDS = 4; // Every x items

    private static final long SMALLEST_DISPLACEMENT_LIMIT = 5; //metres


    public static AppController getInstance() {
        if (instance == null) {
            synchronized (AppController.class) {
                if (instance == null)
                    instance = new AppController();
                instance.component = DaggerAppcontrollerComponent.builder().appControllerModule(new AppControllerModule(instance)).build();
                instance.component.inject(instance);
            }
        }
        // Return the instance
        return instance;
    }


    public AppcontrollerComponent getComponent() {
        return component;
    }

    public User getUser() {
        User user = userService.getRegisteredUser();
        return user;
    }

    public void updateUser(User model) {
        userService.insertUser(model);

    }

    public Setting getSetting() {
        Setting setting = settingService.getRecord();
        return setting;
    }

    public void updateSetting(Setting model) {
        settingService.insert(model);

    }


    @Override
    public void onTrimMemory(final int level) {
        super.onTrimMemory(level);
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) { // Works for Activity
            // Get called every-time when application went to background.
        } else if (level == ComponentCallbacks2.TRIM_MEMORY_COMPLETE) { // Works for FragmentActivty
        }
        inBackground = true;
    }


    public FirebaseAnalytics getFirebaseAnalytics() {
        return mFirebaseAnalytics;
    }

    Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread thread, Throwable ex) {
            Log.e(TAG, "Uncaught exception is: ", ex);
            //  Looper.getMainLooper().getThread() == Thread.currentThread()
            Crashlytics.logException(ex);
            Answers.getInstance().logCustom(new CustomEvent("App crash")
                    .putCustomAttribute("Reason", ex.getMessage().substring(0, Math.min(300, ex.getMessage().length()))));

            // log it & phone home.
            // androidDefaultUEH.uncaughtException(thread, ex);
            Intent intent = new Intent(instance, MainActivity.class);
            PendingIntent pi = PendingIntent.getBroadcast(instance, 0, intent
                    , PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, pi);
            System.exit(2);

        }
    };


    @Override
    public void onCreate() {
        instance = this;
        boolean isDebug = ((this.getApplicationInfo().flags &
                ApplicationInfo.FLAG_DEBUGGABLE) != 0);

        if (isDebug) {//BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }

        super.onCreate();
        component = DaggerAppcontrollerComponent.builder().appControllerModule(new AppControllerModule(this)).build();
        component.inject(this);
        if (instance == null) {
            instance = this;
        }

        Thread.setDefaultUncaughtExceptionHandler(handler);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null);

        Crashlytics crashlyticsKit = new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build();
        Fabric.with(this, crashlyticsKit);

        // Create a new dispatcher using the Google Play driver.
        dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        // JobScheduler jobScheduler = (JobScheduler)getApplicationContext()
        //          .getSystemService(JOB_SCHEDULER_SERVICE);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        mActivityRecognitionClient = new ActivityRecognitionClient(this);


        telephonyInfo = TelephonyUtil.getInstance(this);

        appChecker = new AppChecker();
        // String packageName = appChecker.getForegroundApp(context);
        createUserOnFirstRun();

        checkAndRequestPerms();

        // Create your sync account
        AuthenticatorService.createSyncAccount(this);

        // Perform a manual sync by calling this:
        SyncAdapter.performSync();

        firebaseSetup();

        // component = DaggerAppcontrollerComponent.builder().appModule(new AppModule(this)).build();

        //  DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,"users-db"); //The users-db here is the name of our database.
        //  Database db = helper.getWritableDb();
        //   daoSession = new DaoMaster(db).newSession();

        ///// Using the below lines of code we can toggle ENCRYPTED to true or false in other to use either an encrypted database or not.
//      DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, ENCRYPTED ? "users-db-encrypted" : "users-db");
//      Database db = ENCRYPTED ? helper.getEncryptedWritableDb("super-secret") : helper.getWritableDb();
//      daoSession = new DaoMaster(db).newSession();

// do this once, for example in your Application class
        //       boxStore = MyObjectBox.builder().androidContext(AppController.this).build();
// do this in your activities/fragments to get hold of a Box
        // notesBox = ((AppController) getApplication()).getBoxStore().boxFor(Note.class);

        //requestLocationUpdates(this.getSetting().locationUpdateInterval * 1000);

        Setting setting = getSetting();
        setUSSDAlarm(setting.disableDatabalanceCheck, setting.getDatabalanceCheckTimeInMilli());
        requestActivityRecognition(setting.locationUpdateInterval * 1000);

    }


    @Override
    public void onTerminate() {
        this.stopService(restartServiceIntent);
        Log.i(TAG, "onTerminate!");
        super.onTerminate();

    }

    public void checkAndRequestPerms() {

        Context c = this;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {

                    if (PermissionsUtils.checkAllPermissionsGrantedAndRequestIfNot(c)) {
                        if (!PermissionsUtils.isLocationOn(c)) {

                            Intent intent = new Intent(c, PermissionActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//| Intent.FLAG_ACTIVITY_NO_HISTORY
                            //  intent.putExtra("forceUpdate", forceUpdate);
                            c.startActivity(intent);

                        }
                        ;

                    }

                } catch (Exception e) {
                    Crashlytics.logException(e);
                }
            }
        });

    }

    public JSONObject deviceInfo() {
        try {

            JSONObject JSONObject = new JSONObject();


            String s = "" + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
            JSONObject.put("OS Version", s);

            s = "" + android.os.Build.VERSION.SDK_INT;
            JSONObject.put("OS API Level", s);
            s = "" + android.os.Build.DEVICE;
            JSONObject.put("Device", s);
            s = "" + android.os.Build.MODEL + " (" + android.os.Build.PRODUCT + ")";
            JSONObject.put("Model (and Product)", s);

            TelephonyManager telephonyManager = ((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE));
            s = telephonyManager.getNetworkOperatorName();
            JSONObject.put("Operator", s);


            String myAndroidDeviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            JSONObject.put("myAndroidDeviceId", myAndroidDeviceId);

            if (telephonyInfo.networkSIM0 != null) {
                JSONObject.put("networkSIM0", telephonyInfo.networkSIM0);
                JSONObject.put("telephoneDataSIM0", telephonyInfo.telephoneDataSIM0);
            }
            if (telephonyInfo.networkSIM1 != null) {
                JSONObject.put("networkSIM1", telephonyInfo.networkSIM1);
                JSONObject.put("telephoneDataSIM1", telephonyInfo.telephoneDataSIM1);
            }

            Double batteryCapacity = Utils.getBatteryCapacity(this);
            if (batteryCapacity != null)
                JSONObject.put("batteryCapacity", batteryCapacity);

            s = Utils.getInstalledApps(this);
            JSONObject.put("apps", s);

            Crashlytics.log(Log.DEBUG, TAG, JSONObject.toString());
            return JSONObject;
        } catch (Exception e) {
            Log.e(TAG, "", e);
            Crashlytics.logException(e);
            return null;
        }

    }


    public Job createJob(Class<? extends JobService> serviceClass, String tag, int runAfterSeconds,
                         boolean recurring, Bundle myExtrasBundle) {
        // Bundle myExtrasBundle = new Bundle();
        //  myExtrasBundle.putString("some_key", "some_value");


        int toleranceInterval = (int) TimeUnit.MINUTES.toSeconds(1); // a small(ish) window of time when triggering is OK

        Job job = dispatcher.newJobBuilder()
                // the LocationJobService that will be called
                .setService(serviceClass)
                // uniquely identifies the job
                //  .setTag(serviceClass.getName() + "-" + runAfterSeconds)//make job names unique .. due to replace current
                .setTag(tag)
                // one-off job
                .setRecurring(recurring)
                // don't persist past a device reboot
                .setLifetime(Lifetime.FOREVER)//| Lifetime.UNTIL_NEXT_BOOT
                // Run between xx - xy seconds from now.
                .setTrigger(Trigger.executionWindow(runAfterSeconds, runAfterSeconds + toleranceInterval))
                //.setTrigger(Trigger.executionWindow(15, toleranceInterval))
                // don't overwrite an existing job with the same tag
                .setReplaceCurrent(true)
                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                // constraints that need to be satisfied for the job to run
                //    .setConstraints(
                //           // only run on an unmetered network
                ////          Constraint.ON_UNMETERED_NETWORK,
                // only run when the device is charging
                //           Constraint.DEVICE_CHARGING
                //    )
                .setExtras(myExtrasBundle)
                .build();

        return job;
    }


    //to view scheduled jobs:  adb shell dumpsys activity service GcmService
//    void schedulerJobs() {
//
//        dispatcher.cancelAll();
//
//        // ArrayList<Job> jobs = new ArrayList();
//        //ussd job start midnight
//
//        Calendar calendar = Calendar.getInstance();
//
//        //location job start next hour
//        long currentTsec = calendar.getTimeInMillis() / 1000;
//        Log.d(TAG, "Current Tsec: " + currentTsec);
//
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        //calendar.set(Calendar.MILLISECOND, 0);
//        calendar.add(Calendar.HOUR_OF_DAY, 1);
//        long nextHourTsec = calendar.getTimeInMillis() / 1000;
//        int nextIn = (int) (nextHourTsec - currentTsec);
//
//        Log.d(TAG, "next hour Tsec: " + nextIn);
//
//        Bundle myExtrasBundle = new Bundle();
//
//        myExtrasBundle.putString(JobSchedulerService.JOB_NAME, LocationJobService.class.getName());
//        Job locationjob = createJob(JobSchedulerService.class, LocationJobService.class.getName(), nextIn, false, myExtrasBundle);
//        dispatcher.mustSchedule(locationjob);
//        //jobs.add(locationjob);
//
//
////        //ussd job start next x minute - for testing
////        calendar = Calendar.getInstance();
////        currentTsec = calendar.getTimeInMillis() / 1000;
////        Log.e(TAG, "Current Tsec: " + currentTsec);
////        calendar.set(Calendar.SECOND, 0);
////        calendar.add(Calendar.MINUTE, 2);
////        long minuteTsec = calendar.getTimeInMillis() / 1000;
////        nextIn = (int) (minuteTsec - currentTsec);
////
////        Log.d(TAG, "next minute Tsec: " + nextIn);
////
////        myExtrasBundle = new Bundle();
////
////        myExtrasBundle.putString(JobSchedulerService.JOB_NAME, USSDJobService.class.getName());
////        Job USSDMinjob = createJob(JobSchedulerService.class, USSDJobService.class.getName(), nextIn, false, myExtrasBundle);
////        dispatcher.mustSchedule(USSDMinjob); // jobs.add(USSDMinjob);
//        //return jobs;
//
//
//
//    }

    //adb shell dumpsys alarm
    //https://stackoverflow.com/questions/28742884/how-to-read-adb-shell-dumpsys-alarm-output
    public void setUSSDAlarm(boolean disableDatabalanceCheck, Long alarmTime) {

        try {

            PermissionsUtils.requestBatteryOptimisation(this.getApplicationContext());


            if (alarmTime == null) {
                //    TimeZone timeZone = TimeZone.getTimeZone("UTC");
                Calendar calendar = Calendar.getInstance();//(timeZone);

                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, 7);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                alarmTime = calendar.getTimeInMillis();
            }


            //TODO remove
            //  calendar.setTime(new Date());
            // calendar.add(Calendar.MINUTE, 2);

            Calendar now = Calendar.getInstance();//timeZone);
            long nowTime = now.getTimeInMillis();

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(alarmTime);
            if (nowTime > calendar.getTimeInMillis()) {
                calendar.add(Calendar.DATE, 1);//schedule for tomorrow
                alarmTime = calendar.getTimeInMillis();
            }

            Intent i = new Intent(this.getApplicationContext(), USSDBalanceBroadcastReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(this.getApplicationContext(), 0, i
                    , PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

            //   pi.cancel();
            try {
                am.cancel(pi);
            } catch (Exception e) {
                Crashlytics.log(Log.DEBUG, TAG, e.getMessage());
                Crashlytics.logException(e);
            }

            if (!disableDatabalanceCheck) {
                if (Build.VERSION.SDK_INT >= 23) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                            alarmTime, pi);
                } else if (Build.VERSION.SDK_INT >= 19) {
                    am.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pi);
                }// else {am.set(AlarmManager.RTC_WAKEUP,alarmTime, pi); }

                //hack to force a launch
                Calendar yesterday = Calendar.getInstance();//(timeZone);
                yesterday.setTimeInMillis(alarmTime);//alarmtime
                yesterday.add(Calendar.DATE, -1);
                Setting setting = getSetting();
                if (setting.lastUSSDRun == null || setting.lastUSSDRun.before(yesterday.getTime())) {
                    pi.send(this.getApplicationContext(), 0, i);
                }

            }
            Crashlytics.log(Log.DEBUG, TAG, "Alarm set: " + new Date(alarmTime));
        } catch (Exception e) {
            Crashlytics.log(Log.DEBUG, TAG, e.getMessage());
            Crashlytics.logException(e);
        }
    }

    void createUserOnFirstRun() {

        Setting setting = settingService.getRecord();
        if (setting == null) {
            setting = new Setting();

            settingService.insert(setting);
        }

        //if 1st run - no user record exists.
        User user = userService.getRegisteredUser();
        if (user == null) {
            user = new User();
            //add device info
            String androidId = Utils.getAndroidId(this);
            user.androidId = androidId;
            Crashlytics.setUserIdentifier(user.androidId);
            user.createdAt = new Date();
            if (userService.insertUser(user)) {
                Crashlytics.log("created user");
            } else {
                Crashlytics.log("error creating user information");
                //should crash app
                throw new RuntimeException("unable to create user");
            }
        }

        //TODO:remove this hack after all devices update
        {

            userService.insertUser(user);
        }
        JSONObject JSONObject = deviceInfo();
        user.deviceInfo = JSONObject; //== null ? null : JSONObject.toString();
        userService.insertUser(user);

        logUser(user);

    }

    public void logUser(User user) {
        // TODO: Use the current user's information
        // You can call any combination of these three methods
        Crashlytics.setUserIdentifier(user.androidId);
        //  Crashlytics.setUserEmail("user@fabric.io");
        Crashlytics.setUserName(user.username);

        Answers.getInstance().logCustom(new CustomEvent("App launch")
                .putCustomAttribute("Reason", ""));
        logger.debug("App launch");
        Crashlytics.log(Log.DEBUG, TAG, "App launch");
    }

    @SuppressLint("MissingPermission")
    public void requestLocationUpdates() {
        try {
            long updateInterval = this.getSetting().locationUpdateInterval / 1 * 1000;
            Crashlytics.log(Log.DEBUG, TAG, "requestLocationUpdates " + updateInterval);
            LocationRequest mLocationRequest = createLocationRequest(updateInterval);
            Task<Void> locationTask = mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent(getApplicationContext()));

        } catch (Exception ee) {
            // Log.wtf(TAG, e);
            Crashlytics.logException(ee);
        }
    }

    public void requestLocationUpdates(Activity c) {//(long updateInterval) {

        try {
            long updateInterval = this.getSetting().locationUpdateInterval / 1 * 1000;
            Crashlytics.log(Log.DEBUG, TAG, "requestLocationUpdates " + updateInterval);

            mFusedLocationClient.removeLocationUpdates(getPendingIntent(this.getApplicationContext()));

            LocationRequest mLocationRequest = createLocationRequest(updateInterval);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);
            //.addLocationRequest(mLocationRequestBalancedPowerAccuracy);
            // builder.setNeedBle(true);

            Task<LocationSettingsResponse> task =
                    LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());


            // Block on a task and get the result synchronously. This is generally done
            // when executing a task inside a separately managed background thread. Doing this
            // on the main (UI) thread can cause your application to become unresponsive.
            LocationSettingsResponse response = Tasks.await(task);
            //   latch.countDown();
            //latch.await();
            // All location settings are satisfied. The client can initialize location
            // requests here.
            requestLocationUpdates();

        } catch (Exception e) {
            Crashlytics.logException(e);
            try {
                // Cast to a resolvable exception.
                ResolvableApiException resolvable = (ResolvableApiException) e.getCause();
                // Show the dialog by calling startResolutionForResult(),
                // and check the result in onActivityResult().
                resolvable.startResolutionForResult(c, 1);
            } catch (IntentSender.SendIntentException ee) {
                // Ignore the error.
            } catch (Exception ee) {
                // Log.wtf(TAG, e);
                Crashlytics.logException(ee);
            }

        }
    }

    @SuppressLint("MissingPermission")
    public Location getLastLocation() {
        try {
            requestLocationUpdates();
            Task<Location> task = mFusedLocationClient.getLastLocation();
            //CountDownLatch latch = new CountDownLatch(1);
            //     CyclicBarrier barrier = new CyclicBarrier(1);
            // Block on a task and get the result synchronously. This is generally done
            // when executing a task inside a separately managed background thread. Doing this
            // on the main (UI) thread can cause your application to become unresponsive.
            Location location = Tasks.await(task);
            //   latch.countDown();
            //latch.await();
            return location;
        } catch (Exception e) {
            Crashlytics.logException(e);
            return null;
        }
    }

    /**
     * better way to monitor only when user is moving /active. save battery if asleep?
     *
     * @param updateInterval
     */
    public void requestActivityRecognition(long updateInterval) {
        try {
            Crashlytics.log(Log.DEBUG, TAG, "requestActivityRecognition " + updateInterval);

            Intent mIntentService = new Intent(this, ActivityUpdatesService.class);
            PendingIntent mPendingIntent = PendingIntent.getService(this, 0, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT);

            Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                    updateInterval,
                    mPendingIntent);
        } catch (SecurityException e) {
            // Log.wtf(TAG, e);
            Crashlytics.logException(e);
        }
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    public LocationRequest createLocationRequest(long updateInterval) {
        LocationRequest mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        // Note: apps running on "O" devices (regardless of targetSdkVersion) may receive updates
        // less frequently than this interval when the app is no longer in the foreground.

        long fastestUpdateInterval = updateInterval;// / 2;

        mLocationRequest.setInterval(updateInterval);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(fastestUpdateInterval);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//ocationRequest.PRIORITY_HIGH_ACCURACY


        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.

        long maxWaitTime = updateInterval * MAX_WAIT_RECORDS;
        mLocationRequest.setMaxWaitTime(maxWaitTime);

        mLocationRequest.setNumUpdates(2);//self destruct after this

        mLocationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT_LIMIT);//metres
        return mLocationRequest;
    }

    private PendingIntent getPendingIntent(Context context) {
        // Note: for apps targeting API level 25 ("Nougat") or lower, either
        // PendingIntent.getService() or PendingIntent.getBroadcast() may be used when requesting
        // location updates. For apps targeting API level O, only
        // PendingIntent.getBroadcast() should be used. This is due to the limits placed on services
        // started in the background in "O".

        // TODO(developer): uncomment to use PendingIntent.getService().
        //  Intent intent = new Intent(this, LocationUpdatesIntentService.class);
        //  intent.setAction(LocationUpdatesIntentService.ACTION_PROCESS_UPDATES);
        //  return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent = new Intent(context, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getPendingActivityIntent(Context context) {

        Intent intent = new Intent(context, LocationUpdatesBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    void firebaseSetup() {
        // Get token
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            //Log.w(TAG, "getInstanceId failed", task.getException());
                            Crashlytics.logException(task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Crashlytics.log(Log.DEBUG, TAG, token);

                        User user = getUser();
                        user.fcmToken = token;
                        updateUser(user);


                    }
                });

        FirebaseMessaging.getInstance().subscribeToTopic("all");
    }

}
