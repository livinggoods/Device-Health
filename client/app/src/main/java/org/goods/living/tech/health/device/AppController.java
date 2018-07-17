package org.goods.living.tech.health.device;

import android.Manifest;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.rvalerio.fgchecker.AppChecker;

import org.goods.living.tech.health.device.models.Setting;
import org.goods.living.tech.health.device.models.User;
import org.goods.living.tech.health.device.services.JobSchedulerService;
import org.goods.living.tech.health.device.services.LocationJobService;
import org.goods.living.tech.health.device.services.SettingService;
import org.goods.living.tech.health.device.services.USSDJobService;
import org.goods.living.tech.health.device.services.UserService;
import org.goods.living.tech.health.device.utils.AuthenticatorService;
import org.goods.living.tech.health.device.utils.Constants;
import org.goods.living.tech.health.device.utils.DataBalanceHelper;
import org.goods.living.tech.health.device.utils.LocationUpdatesBroadcastReceiver;
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

    final String TAG = this.getClass().getSimpleName();//BaseService.class.getSimpleName();
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    UserService userService;
    @Inject
    SettingService settingService;

    public TelephonyUtil telephonyInfo;

    public AppChecker appChecker;


    private static final long MAX_WAIT_RECORDS = 2; // Every 5 items

    private static final long SMALLEST_DISPLACEMENT_LIMIT = 10; //metres


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

    public Setting getSetting() {
        Setting setting = settingService.getRecord();
        return setting;
    }

    public void updateSetting(Setting model) {
        settingService.insert(model);

    }


    public FirebaseAnalytics getFirebaseAnalytics() {
        return mFirebaseAnalytics;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerAppcontrollerComponent.builder().appControllerModule(new AppControllerModule(this)).build();
        component.inject(this);
        if (instance == null) {
            instance = this;
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null);

        Fabric.with(this, new Crashlytics());

        // Create a new dispatcher using the Google Play driver.
        dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        telephonyInfo = TelephonyUtil.getInstance(this);

        appChecker = new AppChecker();
        // String packageName = appChecker.getForegroundApp(context);
        createUserOnFirstRun();


        appChecker
                .whenAny(new AppChecker.Listener() {
                    @Override
                    public void onForeground(String packageName) {
                        // do something
                        Crashlytics.log(Log.DEBUG, TAG, "foreground " + packageName);

                        if (Utils.isSmartHealthApp(packageName)) {
                            Crashlytics.log(Log.DEBUG, TAG, "foreground smarthealth: " + packageName);


                            requestLocationUpdates(getUser().updateInterval);
                        }
                    }
                }).timeout((int) getUser().updateInterval * 1000).start(this);


        schedulerJobs();


        checkAndRequestPerms();


        // Create your sync account
        AuthenticatorService.createSyncAccount(this);

        // Perform a manual sync by calling this:
        SyncAdapter.performSync();

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

        requestLocationUpdates(this.getUser().updateInterval);

    }

    public boolean checkAndRequestPerms() {
        if (PermissionsUtils.checkAllPermissionsGrantedAndRequestIfNot(this)) {
            if (PermissionsUtils.checkAllSettingPermissionsGrantedAndRequestIfNot(this)) {
                return true;
            }
        }
        return false;
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

            if (telephonyInfo.networkSIM1 != null) {
                JSONObject.put("networkSIM1", telephonyInfo.networkSIM1);
                JSONObject.put("telephoneDataSIM1", telephonyInfo.telephoneDataSIM1);
            }
            if (telephonyInfo.networkSIM2 != null) {
                JSONObject.put("networkSIM2", telephonyInfo.networkSIM2);
                JSONObject.put("telephoneDataSIM2", telephonyInfo.telephoneDataSIM2);
            }

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


    public Job createJob(Class<? extends JobService> serviceClass, int runAfterSeconds,
                         boolean recurring, Bundle myExtrasBundle) {
        // Bundle myExtrasBundle = new Bundle();
        //  myExtrasBundle.putString("some_key", "some_value");


        int toleranceInterval = (int) TimeUnit.MINUTES.toSeconds(1); // a small(ish) window of time when triggering is OK

        Job job = dispatcher.newJobBuilder()
                // the LocationJobService that will be called
                .setService(serviceClass)
                // uniquely identifies the job
                .setTag(serviceClass.getName() + "-" + runAfterSeconds)//make job names unique .. due to replace current
                // one-off job
                .setRecurring(recurring)
                // don't persist past a device reboot
                .setLifetime(Lifetime.FOREVER)
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

    void schedulerJobs() {

        dispatcher.cancelAll();

        // ArrayList<Job> jobs = new ArrayList();
        //ussd job start midnight


        Calendar calendar = Calendar.getInstance();
        long currentTsec = calendar.getTimeInMillis() / 1000;
        currentTsec = calendar.getTimeInMillis() / 1000;
        Log.e(TAG, "Current Tsec: " + currentTsec);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DATE, 1);
        long midnightTsec = calendar.getTimeInMillis() / 1000;
        int nextIn = (int) (midnightTsec - currentTsec);

        Log.e(TAG, "next midnight Tsec: " + nextIn);

        Bundle myExtrasBundle = new Bundle();

        myExtrasBundle.putString(JobSchedulerService.JOB_NAME, USSDJobService.class.getName());
        Job USSDjob = createJob(JobSchedulerService.class, nextIn, false, myExtrasBundle);
        dispatcher.mustSchedule(USSDjob);
        //jobs.add(USSDjob);//


        //location job start next hour
        currentTsec = calendar.getTimeInMillis() / 1000;
        Log.e(TAG, "Current Tsec: " + currentTsec);

        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        //calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        long nextHourTsec = calendar.getTimeInMillis() / 1000;
        nextIn = (int) (nextHourTsec - currentTsec);

        Log.e(TAG, "next hour Tsec: " + nextIn);

        myExtrasBundle = new Bundle();

        myExtrasBundle.putString(JobSchedulerService.JOB_NAME, LocationJobService.class.getName());
        Job locationjob = createJob(JobSchedulerService.class, nextIn, false, myExtrasBundle);
        dispatcher.mustSchedule(locationjob);
        //jobs.add(locationjob);


        //ussd job start next x minute - for testing
        calendar = Calendar.getInstance();
        currentTsec = calendar.getTimeInMillis() / 1000;
        Log.e(TAG, "Current Tsec: " + currentTsec);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.MINUTE, 2);
        long minuteTsec = calendar.getTimeInMillis() / 1000;
        nextIn = (int) (minuteTsec - currentTsec);

        Log.e(TAG, "next minute Tsec: " + nextIn);

        myExtrasBundle = new Bundle();

        myExtrasBundle.putString(JobSchedulerService.JOB_NAME, USSDJobService.class.getName());
        Job USSDMinjob = createJob(JobSchedulerService.class, nextIn, false, myExtrasBundle);
        dispatcher.mustSchedule(USSDMinjob); // jobs.add(USSDMinjob);
        //return jobs;
    }

    void createUserOnFirstRun() {

        Setting setting = settingService.getRecord();
        if (setting == null) {
            setting = new Setting();
            setting.fetchingUSSD = false;
            setting.workingUSSD0 = DataBalanceHelper.USSDList;
            setting.workingUSSD1 = DataBalanceHelper.USSDList;

            settingService.insert(setting);
        }
        setting.fetchingUSSD = false;
        //if 1st run - no user record exists.
        User user = userService.getRegisteredUser();
        if (user == null) {
            user = new User();
            user.updateInterval = Constants.UPDATE_INTERVAL;
            //add device info
            String androidId = Utils.getAndroidId(this);
            user.androidId = androidId;
            Crashlytics.setUserIdentifier(user.androidId);
            user.createdAt = new Date();
            if (userService.insertUser(user)) {
                Crashlytics.log("created user");
            } else {
                Crashlytics.log("error creating user information");
            }
        }

        //TODO:remove this hack after all devices update
        {
            user.forceUpdate = false;
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


    /**
     * request updates if not already setup
     */
    public void requestLocationUpdates(long updateInterval) {
        try {

            //   if (forceUpdate) {
            LocationRequest mLocationRequest = createLocationRequest(updateInterval);
            Task<Void> locationTask = mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent(this.getApplicationContext()));
            //Log.d(TAG, "locationTask " + locationTask.isSuccessful());
            //  }


        } catch (SecurityException e) {
            Log.wtf(TAG, e);
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
    private LocationRequest createLocationRequest(long updateInterval) {
        LocationRequest mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        // Note: apps running on "O" devices (regardless of targetSdkVersion) may receive updates
        // less frequently than this interval when the app is no longer in the foreground.

        long fastestUpdateInterval = updateInterval / 2;

        mLocationRequest.setInterval(updateInterval);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(fastestUpdateInterval);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);//ocationRequest.PRIORITY_HIGH_ACCURACY

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.

        long maxWaitTime = updateInterval * MAX_WAIT_RECORDS;
        mLocationRequest.setMaxWaitTime(maxWaitTime);

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

    public Task<Location> getlastKnownLocation() {

        if (android.os.Build.VERSION.SDK_INT > 22) { /*Ask Dungerous Permissions here*/
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                return mFusedLocationClient.getLastLocation();
            } else
                return null;
        } else {
            return mFusedLocationClient.getLastLocation();
        }

    }


}
