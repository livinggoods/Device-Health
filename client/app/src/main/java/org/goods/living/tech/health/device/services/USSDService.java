package org.goods.living.tech.health.device.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import com.crashlytics.android.Crashlytics;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.BuildConfig;
import org.goods.living.tech.health.device.utils.DataBalanceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;


public class USSDService extends AccessibilityService {
    public static String TAG = USSDService.class.getSimpleName();


    @Inject
    DataBalanceHelper dataBalanceHelper;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            Crashlytics.log(Log.DEBUG, TAG, "onAccessibilityEvent");

            AppController.getInstance().getComponent().inject(this);


            if (dataBalanceHelper.fullDialTimeComplete()) {
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
//                if (inputNode != null) { //prepare you text then fill it using ACTION_SET_TEXT
//                    Bundle arguments = new Bundle();
//
//                    String in = getNextUSSDInput();
//
//
//                    Crashlytics.log(Log.DEBUG, TAG, "-------------------------------------------------");
//                    Crashlytics.log(Log.DEBUG, TAG, "Checking balance (Running USSD)");
//                    Crashlytics.log(Log.DEBUG, TAG, "-------------------------------------------------");
//                    arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, in);
//                    inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
//                    //}
//                }
//                //"Click" the Send button
//                List<AccessibilityNodeInfo> list = source.findAccessibilityNodeInfosByText("Send");
//                for (AccessibilityNodeInfo node : list) {
//                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                }


                //"Click" the Ok button
                List<AccessibilityNodeInfo> okButton = source.findAccessibilityNodeInfosByText("OK");
                for (AccessibilityNodeInfo node : okButton) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                if (okButton != null && !okButton.isEmpty()) {//last message?

                    if (getListener() != null) {
                        getListener().onUSSDReceived(text);
                    }
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

        try {
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

        } catch (Exception e) {
            // Log.e(TAG, e.toString());
            Crashlytics.logException(e);
            return false;
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

    static void logInstalledAccessiblityServices(Context context) {

        AccessibilityManager am = (AccessibilityManager) context
                .getSystemService(Context.ACCESSIBILITY_SERVICE);

        List<AccessibilityServiceInfo> runningServices = am
                .getInstalledAccessibilityServiceList();
        for (AccessibilityServiceInfo service : runningServices) {
            Crashlytics.log(Log.DEBUG, TAG, service.getId());
            //Crashlytics.log(int priority, String tag, String msg)
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


//    public static String getNextUSSDInput() {
//
//        if (ussdInputList != null && ussdInputList.size() > 0) {
//            String code = ussdInputList.get(0);
//            ussdInputList.remove(0);
//
//            return code;
//        }
//        return null;
//    }
//
//    public static String getUSSDCode(String full) {
//
//        ussdInputList = getUSSDCodesFromString(full);
//        String code = ussdInputList.get(0);
//        ussdInputList.remove(0);
//
//        if (ussdInputList.isEmpty()) {
//            ussdInputList = null;
//        }
//        return code;
//
//    }

    //from comma separated list of codes
    public static ArrayList<String> getUSSDCodesFromString(String full) {

        if (full == null) {
            return null;
        }
        ArrayList<String> list = new ArrayList<>(Arrays.asList(full.split(",[ ]*")));
        return list;

    }

    public interface USSDListener {
        void onUSSDReceived(String raw);
    }


    static USSDListener listener;

    public static USSDListener getListener() {
        return listener;
    }


    public static void bindListener(USSDListener list) {
        listener = list;
    }

    public static void unbindListener() {
        listener = null;
    }
}