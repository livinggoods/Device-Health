package org.goods.living.tech.health.device.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import com.crashlytics.android.Crashlytics;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.BuildConfig;
import org.goods.living.tech.health.device.models.User;
import org.goods.living.tech.health.device.utils.PermissionsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;


public class USSDService extends AccessibilityService {
    public static String TAG = USSDService.class.getSimpleName();
    public static String MTN_TAG = "balance";

    @Inject
    UserService userService;

    @Inject
    DataBalanceService dataBalanceService;
    static List<String> ussdInputList;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            Log.d(TAG, "onAccessibilityEvent");

            AppController.getInstance().getComponent().inject(this);


            if (!isUSSDPopupAppTriggered()) {
                Log.d(TAG, "not our ussd");
                return;
            } else {
                if (ussdInputList.isEmpty()) { //reset
                    ussdInputList = null;


                }
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
            Log.d(TAG, text);
            saveUssdMessage(text);


            Log.d(TAG, "Setting the Input");
            //  AccessibilityNodeInfo source = event.getSource();
            if (!source.getPackageName().equals(BuildConfig.APPLICATION_ID)) {//source != null &&
                Log.d(TAG, "SOURCE IS FOUND");

                //capture the EditText simply by using FOCUS_INPUT (since the EditText has the focus), you can probably find it with the viewId input_field
                AccessibilityNodeInfo inputNode = source.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
                if (inputNode != null) { //prepare you text then fill it using ACTION_SET_TEXT
                    Bundle arguments = new Bundle();


                    User user = userService.getRegisteredUser();

                    String in = getNextUSSDInput();


                    Log.d(TAG, "-------------------------------------------------");
                    Log.d(TAG, "Checking balance (Running USSD)");
                    Log.d(TAG, "-------------------------------------------------");
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
        Log.d(TAG, "onServiceConnected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.packageNames = null;//new String[]{"com.android.phone"};"@null"
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
            Log.i(TAG, service.getId());
        }
    }

    public void saveUssdMessage(String ussdMessage) {

        if (ussdMessage != null && ussdMessage.contains(MTN_TAG)) {

            Log.d(TAG, ussdMessage);

            Pattern p = Pattern.compile("-?\\d+\\s?MB");
            Matcher m = p.matcher(ussdMessage);
            while (m.find()) {
                Double d = Double.valueOf(m.group());
                Log.d(TAG, d.toString());
                dataBalanceService.insert(d, ussdMessage);

            }

            if (ussdMessage != null && ussdMessage.contains("SMS")) { //check sms (Safcom)
                performGlobalAction(GLOBAL_ACTION_BACK);

            }

        }

    }

    public static void sendUSSDDirect(Context c, User user) {

        if (PermissionsUtils.checkAllPermissionsGrantedAndRequestIfNot(c)) {
            //  c.startService(new Intent(c.getApplicationContext(), USSDService.class));//not needed


            String ussd = USSDService.getUSSDCode(user.balanceCode);

            dialNumber(c, ussd);
            //well timed ones also work - but how much time ?
            //startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + ussdCode)));
            String nxt = USSDService.getNextUSSDInput();

            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                c.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + nxt)));

                nxt = USSDService.getNextUSSDInput();

            } while (nxt != null);


        } else {
            // ActivityCompat.requestPermissions(c, new String[]{android.Manifest.permission.CALL_PHONE}, 1);
            Log.i(TAG, "USSDService permissions not granted yet ...");
        }
    }


    public static void dialNumber(Context c, String ussd) {
        String ussdCode = ussd.replace("#", "") + Uri.encode("#");
        c.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + ussdCode)));
    }

    public static boolean isUSSDPopupAppTriggered() {
        return (ussdInputList != null);
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

        List<String> list = new ArrayList<>(Arrays.asList(full.split(",[ ]*")));
        ussdInputList = list;
        String code = list.get(0);
        ussdInputList.remove(0);

        return code;

    }

}