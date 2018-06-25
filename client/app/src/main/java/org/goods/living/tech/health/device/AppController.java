package org.goods.living.tech.health.device;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.goods.living.tech.health.device.models.Setting;
import org.goods.living.tech.health.device.models.User;
import org.goods.living.tech.health.device.services.LocationJobService;
import org.goods.living.tech.health.device.services.SettingService;
import org.goods.living.tech.health.device.services.USSDJobService;
import org.goods.living.tech.health.device.services.UserService;
import org.goods.living.tech.health.device.utils.PermissionsUtils;
import org.goods.living.tech.health.device.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.fabric.sdk.android.Fabric;

public class AppController extends Application {

    // Create the instance
    private static AppController instance;

    private AppcontrollerComponent component;

    FirebaseAnalytics mFirebaseAnalytics;

    FirebaseJobDispatcher dispatcher;

    final String TAG = this.getClass().getSimpleName();//BaseService.class.getSimpleName();
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    UserService userService;
    @Inject
    SettingService settingService;

    public final String USSD_KE = "*100*6*6*2#";// "*100*6*4*2#"; "*100#,6,6,2";//"*100*1*1#";
    public final String USSD_UG = "*150*1*4*1#";//"*150*1#,4,1";

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

        dispatcher.mustSchedule(createLocationJob());
        dispatcher.mustSchedule(createUSSDJob());

        createUserOnFirstRun();
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


    }

    public boolean checkAndRequestPerms() {
        if (PermissionsUtils.checkAllPermissionsGrantedAndRequestIfNot(this)) {
            if (PermissionsUtils.checkAllSettingPermissionsGrantedAndRequestIfNot(this)) {
                return true;
            }
        }
        return false;
    }

    public String deviceInfo() {
        try {
            String s = "OS Version: " + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
            s += " OS API Level: " + android.os.Build.VERSION.SDK_INT;
            s += " Device: " + android.os.Build.DEVICE;
            s += " Model (and Product): " + android.os.Build.MODEL + " (" + android.os.Build.PRODUCT + ")";

            Crashlytics.log(Log.DEBUG, TAG, s);
            return s;
        } catch (Exception e) {
            Log.e(TAG, "", e);
            Crashlytics.logException(e);
            return null;
        }

    }

    Job createLocationJob() {
        Bundle myExtrasBundle = new Bundle();
        myExtrasBundle.putString("some_key", "some_value");


        int toleranceInterval = (int) TimeUnit.MINUTES.toSeconds(1); // a small(ish) window of time when triggering is OK

        Job job = dispatcher.newJobBuilder()
                // the LocationJobService that will be called
                .setService(LocationJobService.class)
                // uniquely identifies the job
                .setTag(LocationJobService.class.getName())
                // one-off job
                .setRecurring(true)
                // don't persist past a device reboot
                .setLifetime(Lifetime.FOREVER)
                // Run between xx - xy seconds from now.
                .setTrigger(Trigger.executionWindow(LocationJobService.runEverySeconds, LocationJobService.runEverySeconds + toleranceInterval))
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

    Job createUSSDJob() {
        Bundle myExtrasBundle = new Bundle();
        myExtrasBundle.putString("some_key", "some_value");

        int toleranceInterval = (int) TimeUnit.MINUTES.toSeconds(1); // a small(ish) window of time when triggering is OK

        Job job = dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(USSDJobService.class)
                // uniquely identifies the job
                .setTag(USSDJobService.class.getName())
                // one-off job
                .setRecurring(true)
                // don't persist past a device reboot
                .setLifetime(Lifetime.FOREVER)
                // Run between xx - xy seconds from now.
                // .setTrigger(Trigger.executionWindow(USSDJobService.runEverySeconds, USSDJobService.runEverySeconds + toleranceInterval))
                .setTrigger(Trigger.executionWindow(60, toleranceInterval))

                // don't overwrite an existing job with the same tag
                .setReplaceCurrent(true)
                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                .setExtras(myExtrasBundle)
                .build();

        return job;

    }

    void createUserOnFirstRun() {

        //if 1st run - no user record exists.
        User user = userService.getRegisteredUser();
        if (user == null) {
            user = new User();
            user.updateInterval = PermissionsUtils.UPDATE_INTERVAL;
            //add device info
            String androidId = Utils.getAndroidId(this);
            user.androidId = androidId;
            Crashlytics.setUserIdentifier(user.androidId);
            user.createdAt = new Date();
            if (userService.insertUser(user)) {
                Crashlytics.log("created user settings");
            } else {
                Crashlytics.log("error creating user information");
            }
        }

        //TODO:remove this hack after all devices update
        {
            user.forceUpdate = false;
            userService.insertUser(user);
        }

        String deviceInfo = deviceInfo();
        user.deviceInfo = deviceInfo;

        //TODO:remove this hack after all devices update
        if (user.balanceCode == null) { //try deduce country

            if (user.phone != null && user.phone.startsWith("+256")) { //ug
                user.balanceCode = USSD_UG;
            } else { //ke?
                user.balanceCode = USSD_KE;
            }
            userService.insertUser(user);
        }

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

}
