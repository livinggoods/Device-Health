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
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.services.DataBalanceService;

import javax.inject.Inject;


public class SmsBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = SmsBroadcastReceiver.class.getSimpleName();

    @Inject
    DataBalanceService dataBalanceService;


    private Listener listener;

    public SmsBroadcastReceiver() {

    }


    @Override
    public void onReceive(Context context, Intent intent) {

        AppController appController;
        if (!(context.getApplicationContext() instanceof AppController)) {
            appController = ((AppController) context.getApplicationContext());

        } else {
            appController = AppController.getInstance();

        }
        appController.getComponent().inject(this);


        try {
            Crashlytics.log(Log.DEBUG, TAG, "Sms in");
            context.unregisterReceiver(this);


            if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
                String smsSender = "";
                String smsBody = "";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                        smsSender = smsMessage.getDisplayOriginatingAddress();
                        smsBody += smsMessage.getMessageBody();
                        smsMessage.getIndexOnIcc();
                        smsMessage.getStatus();
                        smsMessage.getMessageBody();
                        smsMessage.getProtocolIdentifier();
                        smsMessage.getOriginatingAddress();
                        smsMessage.getServiceCenterAddress();
                        smsMessage.getUserData();
                    }
                } else {
                    Bundle smsBundle = intent.getExtras();
                    if (smsBundle != null) {
                        Object[] pdus = (Object[]) smsBundle.get("pdus");
                        if (pdus == null) {
                            // Display some error to the user
                            Log.e(TAG, "SmsBundle had no pdus key");
                            return;
                        }
                        SmsMessage[] messages = new SmsMessage[pdus.length];
                        for (int i = 0; i < messages.length; i++) {
                            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                            smsBody += messages[i].getMessageBody();
                        }
                        smsSender = messages[0].getOriginatingAddress();
                    }
                }

                //      if (smsSender.equals(serviceProviderNumber) && smsBody.startsWith(serviceProviderSmsCondition)) {
                this.abortBroadcast();//avoid an incoming sms msg if its our balance check
                Crashlytics.log(Log.DEBUG, TAG, smsBody);
                if (listener != null) {
                    listener.onTextReceived(smsBody);
                }
                //   }
            }
        } catch (
                Exception e)

        {
            Log.d(TAG, e.getMessage());
        }

    }


    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onTextReceived(String text);
    }
}



