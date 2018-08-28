package org.goods.living.tech.health.device.services;

import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.models.Setting;
import org.goods.living.tech.health.device.models.User;
import org.goods.living.tech.health.device.receivers.USSDBalanceBroadcastReceiver;
import org.goods.living.tech.health.device.utils.SyncAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class FirebaseMessageService extends FirebaseMessagingService {

    final String TAG = this.getClass().getSimpleName();

    @Inject
    StatsService statsService;

    @Inject
    SyncService syncService;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    AppController appController;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (!(getApplicationContext() instanceof AppController)) {
            appController = ((AppController) getApplicationContext());

        } else {
            appController = AppController.getInstance();

        }
        appController.getComponent().inject(this);

        // ...

        Answers.getInstance().logCustom(new CustomEvent("Firebase message received")
                .putCustomAttribute("Reason", remoteMessage.getData().toString()));

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Crashlytics.log(Log.INFO, TAG, "Message data payload: " + remoteMessage.getData());

            String function = remoteMessage.getData().get("function");
            function = function.toLowerCase();

            switch (function) {
                case "setting":
                    //startService(new Intent(this, LService.class));
                    syncService.syncSetting();
                    break;

                case "sync":

                    Setting setting = appController.getSetting();
                    setting.disableSync = false;
                    appController.updateSetting(setting);
                    SyncAdapter.performSync();
                    break;
                case "location":
                    appController.requestLocationUpdates(appController.getSetting().locationUpdateInterval);
                    break;
                case "databalance":
                    Intent i = new Intent(this.getApplicationContext(), USSDBalanceBroadcastReceiver.class);
                    sendBroadcast(i);
                    break;
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Crashlytics.log(Log.INFO, TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();


        if (!(getApplicationContext() instanceof AppController)) {
            appController = ((AppController) getApplicationContext());

        } else {
            appController = AppController.getInstance();

        }
        appController.getComponent().inject(this);

        Answers.getInstance().logCustom(new CustomEvent("Firebase message delete"));
        //.putCustomAttribute("Reason", oldToken));

        Crashlytics.log(Log.INFO, TAG, "onDeletedMessages");
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);


        if (!(getApplicationContext() instanceof AppController)) {
            appController = ((AppController) getApplicationContext());

        } else {
            appController = AppController.getInstance();

        }
        appController.getComponent().inject(this);

        Answers.getInstance().logCustom(new CustomEvent("Firebase message newtoken"));
        //.putCustomAttribute("Reason", oldToken));

        Crashlytics.log(Log.INFO, TAG, token);

        User user = appController.getUser();
        user.fcmToken = token;
        appController.updateUser(user);

    }
}