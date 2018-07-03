package org.goods.living.tech.health.device.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import com.crashlytics.android.Crashlytics;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.BuildConfig;
import org.goods.living.tech.health.device.models.DataBalance;
import org.goods.living.tech.health.device.models.Setting;
import org.goods.living.tech.health.device.models.User;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;


public class USSDService extends AccessibilityService {
    public static String TAG = USSDService.class.getSimpleName();
    public static String MTN_TAG = "balance";

    private final static String simSlotName[] = {
            "extra_asus_dial_use_dualsim",
            "com.android.phone.extra.slot",
            "slot",
            "simslot",
            "sim_slot",
            "subscription",
            "Subscription",
            "phone",
            "com.android.phone.DialingMode",
            "simSlot",
            "slot_id",
            "simId",
            "simnum",
            "phone_type",
            "slotId",
            "slotIdx"
    };
    @Inject
    UserService userService;

    @Inject
    DataBalanceService dataBalanceService;
    static List<String> ussdInputList;

    static long dialTime;

    static final int USSD_LIMIT = 3;
    static CountDownTimer timer;

    public final String USSD_KE = "*100*6*6*2#";// "*100*6*4*2#"; "*100#,6,6,2";//"*100*1*1#";
    public final String USSD_UG = "*150*1*4*1#";//"*150*1#,4,1";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            Crashlytics.log(Log.DEBUG, TAG, "onAccessibilityEvent");

            AppController.getInstance().getComponent().inject(this);


            if (!isUSSDPopupAppTriggered()) {
                Crashlytics.log(Log.DEBUG, TAG, "not our ussd");
                return;
            } else {

            }

            AccessibilityNodeInfo source = event.getSource();
            /* if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && !event.getClassName().equals("android.app.AlertDialog")) { // android.app.AlertDialog is the standard but not for all phones  */
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && !String.valueOf(event.getClassName()).contains("AlertDialog")) {
                return;
            }
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && (source == null || !source.getClassName().equals("android.widget.TextView"))) {
                return;
            }
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && TextUtils.isEmpty(source.getText())) {
                return;
            }

            List<CharSequence> eventText;

            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                eventText = event.getText();
            } else {
                eventText = Collections.singletonList(source.getText());
            }

            String text = "";//event.getText().toString();
            for (CharSequence s : eventText) {
                text += String.valueOf(s);
            }
            Crashlytics.log(Log.DEBUG, TAG, text);

            Crashlytics.log(Log.DEBUG, TAG, "Setting the Input");
            //  AccessibilityNodeInfo source = event.getSource();
            if (!source.getPackageName().equals(BuildConfig.APPLICATION_ID)) {//source != null &&
                Crashlytics.log(Log.DEBUG, TAG, "SOURCE IS FOUND");

                //capture the EditText simply by using FOCUS_INPUT (since the EditText has the focus), you can probably find it with the viewId input_field
                AccessibilityNodeInfo inputNode = source.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
                if (inputNode != null) { //prepare you text then fill it using ACTION_SET_TEXT
                    Bundle arguments = new Bundle();


                    User user = userService.getRegisteredUser();

                    String in = getNextUSSDInput();


                    Crashlytics.log(Log.DEBUG, TAG, "-------------------------------------------------");
                    Crashlytics.log(Log.DEBUG, TAG, "Checking balance (Running USSD)");
                    Crashlytics.log(Log.DEBUG, TAG, "-------------------------------------------------");
                    arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, in);
                    inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                    //}
                }
                //"Click" the Send button
                List<AccessibilityNodeInfo> list = source.findAccessibilityNodeInfosByText("Send");
                for (AccessibilityNodeInfo node : list) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }


                //"Click" the Ok button
                List<AccessibilityNodeInfo> okButton = source.findAccessibilityNodeInfosByText("OK");
                for (AccessibilityNodeInfo node : okButton) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                if (okButton != null && !okButton.isEmpty()) {//last message?

                    saveUssdMessage(text);
                }


            }

        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Crashlytics.log(Log.DEBUG, TAG, "onServiceConnected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.packageNames = new String[]{"com.android.phone"}; //null;//new String[]{"com.android.phone"};"@null"
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);

        AppController.getInstance().getComponent().inject(this);


    }

    public static boolean isAccessibilityServiceEnabled(Context context) {

        USSDService.logInstalledAccessiblityServices(context);
        ComponentName expectedComponentName = new ComponentName(context.getApplicationContext(), USSDService.class);

        String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null)
            return false;

        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
        colonSplitter.setString(enabledServicesSetting);

        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            ComponentName enabledService = ComponentName.unflattenFromString(componentNameString);

            if (enabledService != null && enabledService.equals(expectedComponentName))
                return true;
        }

        return false;
    }

    public static boolean isAccessibilityEnabled(Context context, String id) {

        AccessibilityManager am = (AccessibilityManager) context
                .getSystemService(Context.ACCESSIBILITY_SERVICE);

        List<AccessibilityServiceInfo> runningServices = am
                .getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
        for (AccessibilityServiceInfo service : runningServices) {
            if (id.equals(service.getId())) {
                return true;
            }
        }

        return false;
    }

    public static void logInstalledAccessiblityServices(Context context) {

        AccessibilityManager am = (AccessibilityManager) context
                .getSystemService(Context.ACCESSIBILITY_SERVICE);

        List<AccessibilityServiceInfo> runningServices = am
                .getInstalledAccessibilityServiceList();
        for (AccessibilityServiceInfo service : runningServices) {
            Crashlytics.log(Log.DEBUG, TAG, service.getId());
            //Crashlytics.log(int priority, String tag, String msg)
        }
    }

    public void saveUssdMessage(String ussdMessage) {

        //if we r done retrieving ussd
        //   if (ussdInputList != null && ussdInputList.isEmpty()) {
        // if (ussdMessage != null && ussdMessage.contains(MTN_TAG)) {
        try {
            Crashlytics.log(Log.DEBUG, TAG, ussdMessage);

            Pattern p = Pattern.compile("-?\\d+\\s?MB");
            Matcher m = p.matcher(ussdMessage);
            Double total = null;
            while (m.find()) {
                Double d = Double.parseDouble(m.group().replaceAll("[^0-9?!\\.]", ""));
                total += d;
                Crashlytics.log(Log.DEBUG, TAG, "MB: " + d.toString());

            }

            dataBalanceService.insert(total, ussdMessage);

            if (dataBalanceService.getListener() != null) {
                dataBalanceService.getListener().onUSSDReceived(total.toString(), ussdMessage);
            }
            // if (ussdMessage != null && ussdMessage.contains("SMS")) { //check sms (Safcom)
            //     performGlobalAction(GLOBAL_ACTION_BACK);

            // }

            //     }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }
//
//    public static void sendUSSDDirect(Context c, String ussdFull) {
//
//        if (PermissionsUtils.checkAllPermissionsGrantedAndRequestIfNot(c)) {
//            //  c.startService(new Intent(c.getApplicationContext(), USSDService.class));//not needed
//
//
//            String ussd = USSDService.getUSSDCode(ussdFull);
//
//            dialNumber(c, ussd);
//            //well timed ones also work - but how much time ?
//            //startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + ussdCode)));
//            String nxt = USSDService.getNextUSSDInput();
//
//            do {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                c.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + nxt)));
//
//                nxt = USSDService.getNextUSSDInput();
//
//            } while (nxt != null);
//
//
//        } else {
//            // ActivityCompat.requestPermissions(c, new String[]{android.Manifest.permission.CALL_PHONE}, 1);
//            Crashlytics.log(Log.DEBUG, TAG, "USSDService permissions not granted yet ...");
//        }
//    }
//

    public static void dialNumber(Context c, DataBalanceService dbService, String ussd, int port) {
        String ussdCode = ussd.replace("#", "") + Uri.encode("#");

        Intent intent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + ussdCode));

        intent.putExtra("com.android.phone.force.slot", true);
        intent.putExtra("Cdma_Supp", true);
        //Add all slots here, according to device.. (different device require different key so put all together)
        for (String s : simSlotName)
            intent.putExtra(s, port); //0 or 1 according to sim.......

        //works only for API >= 21
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", "");

        timer = new CountDownTimer(USSD_LIMIT * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Crashlytics.log(Log.DEBUG, TAG, "waiting for ussd");
            }

            @Override
            public void onFinish() {
                Crashlytics.log(Log.DEBUG, TAG, "onFinish");
                //check if ussd code works
                Setting setting = AppController.getInstance().getSetting();

                List<DataBalance> l = dbService.getLatestRecords(1l);
                DataBalance d = l.size() > 0 ? l.get(0) : null;
                if (d != null && d.balance != null) {
                    //works save it
                    if (port == 0) {
                        setting.ussd0 = ussd;
                    } else {
                        setting.ussd1 = ussd;
                    }
                    AppController.getInstance().updateSetting(setting);
                }


                //if sim 0 try one too
                if (port == 0) {
                    dialNumber(c, dbService, ussd, 1);

                }
            }
        };
        timer.start();

        c.startActivity(intent);

        Calendar cal = Calendar.getInstance();
        dialTime = cal.getTimeInMillis();
    }

    public static boolean isUSSDPopupAppTriggered() {

        Calendar c = Calendar.getInstance();
        long now = c.getTimeInMillis();
//        c.set(Calendar.HOUR_OF_DAY, 0);
//        c.set(Calendar.MINUTE, 0);
//        c.set(Calendar.SECOND, 0);
//        c.set(Calendar.MILLISECOND, 0);
        long passed = now - dialTime;
        long secondsPassed = passed / 1000;

        return (secondsPassed <= USSD_LIMIT);

    }

    public static String getNextUSSDInput() {

        if (ussdInputList != null && ussdInputList.size() > 0) {
            String code = ussdInputList.get(0);
            ussdInputList.remove(0);

            return code;
        }
        return null;
    }

    public static String getUSSDCode(String full) {

        if (full == null) {
            return null;
        }
        List<String> list = new ArrayList<>(Arrays.asList(full.split(",[ ]*")));
        ussdInputList = list;
        String code = list.get(0);
        ussdInputList.remove(0);

        if (ussdInputList.isEmpty()) {
            ussdInputList = null;
        }
        return code;

    }

    public static void telephonyManagerMethodNamesForThisDevice(Context context) {

        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        Class<?> telephonyClass;
        try {
            telephonyClass = Class.forName(telephony.getClass().getName());
            Method[] methods = telephonyClass.getMethods();
            for (int idx = 0; idx < methods.length; idx++) {

                Crashlytics.log(Log.DEBUG, TAG, methods[idx] + " declared by " + methods[idx].getDeclaringClass());

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}