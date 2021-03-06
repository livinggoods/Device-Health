package org.goods.living.tech.health.device.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.goods.living.tech.health.device.receivers.SmsBroadcastReceiver;
import org.goods.living.tech.health.device.services.DataBalanceService;
import org.goods.living.tech.health.device.services.USSDService;
import org.goods.living.tech.health.device.services.UserService;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DataBalanceHelper {

    public String TAG = this.getClass().getSimpleName();


    @Inject
    public DataBalanceHelper() {

        smsBroadcastReceiver = new SmsBroadcastReceiver();
    }

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
    List<String> ussdInputList;

    long dialTime;

    public static final int USSD_LIMIT = 40;//in seconds

    CountDownTimer timer;

    // public static final String USSD_KE_SAF = "*544*44#";//"*100*6*6*2#";// "*100*6*4*2#"; "*100#,6,6,2";//"*100*1*1#";
    //public static final String USSD_KE2 = "*100*6*4*2#";
    //  public static final String USSD_UG_MTN = "*150*1*4*1#";//"*150*1#,4,1";
    //  public static final List<String> USSDList = Arrays.asList(USSD_KE_SAF, USSD_UG_MTN);

    SmsBroadcastReceiver smsBroadcastReceiver;


    public Double extractBalance(String rawText) {

        Double total = null;
        try {
            Pattern p = Pattern.compile(USSDService.bundlePattern);
            Matcher m = p.matcher(rawText);

            while (m.find()) {
                String s = m.group();
                s = s.replaceAll("[^\\d.]", "");
                Double d = Double.parseDouble(s);
                if (d != null)
                    total = total == null ? d : (total + d);
                Crashlytics.log(Log.DEBUG, TAG, "MB: " + d);

            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        return total;
    }

    public Date extractExpiry(String rawText) {

        List<String> separators = new ArrayList<String>();
        separators.add(".");
        separators.add("-");
        separators.add("/");
        separators.add("\\");
        // knownPatterns.add(new SimpleDateFormat("dd-mm-yyyy"));
        // String regex = "^[0-3]?[0-9]/[0-3]?[0-9]/(?:[0-9]{2})?[0-9]{2}$";// – allow leading zeros to be omitted  01/01/2011
        String regex1 = "\\d{2}(\\.|-|/|\\\\)\\d{2}(\\.|-|/|\\\\)\\d{4}";//dd/mm/yyyy
        String regex2 = "\\d{4}(\\.|-|/|\\\\)\\d{2}(\\.|-|/|\\\\)\\d{2}";//yyyy/mm/dd  separators://.-/\

        String regTemplate1 = "dd{sep}mm{sep}yyyy";
        String regTemplate2 = "yyyy{sep}mm{sep}dd";


        try {
            Matcher m;
            String regex = "";

            Pattern p1 = Pattern.compile(regex1);
            Matcher m1 = p1.matcher(rawText);
            Pattern p2 = Pattern.compile(regex2);
            Matcher m2 = p2.matcher(rawText);

            if (m1.find()) {
                m = m1;
                regex = regTemplate1;

            } else {
                m = m2;
                regex = regTemplate2;
            }
            String s = m.group();
            // String[] elements = rawText.split(" ");
            for (String sep : separators) {
                try {
                    SimpleDateFormat pattern = new SimpleDateFormat(regex.replace("{sep}", sep));
                    Date d = new Date(pattern.parse(s).getTime());

                    return d;
                } catch (ParseException pe) {
                    // Loop on
                }
            }
            return null;

        } catch (Exception e) {
            Crashlytics.logException(e);
            return null;
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

    public boolean dialTimeComplete() {

        Calendar c = Calendar.getInstance();
        long now = c.getTimeInMillis();
        long passed = now - dialTime;
        long secondsPassed = passed / 1000;

        return (secondsPassed >= USSD_LIMIT);

    }

    public void setDialTimeComplete() {
        dialTime = dialTime - (USSD_LIMIT * 1000);

    }


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
    public static List<String> getUSSDCodesFromString(String full) {

        if (full == null) {
            return null;
        }
        List<String> list = new ArrayList<>(Arrays.asList(full.split(",[ ]*")));
        return list;

    }

    public void telephonyManagerMethodNamesForThisDevice(Context context) {

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

    public interface USSDResult {
        void onResult(@NonNull Balance bal);
    }

    public class Balance {

        public int port;
        public Double balance;
        public String rawBalance;
        public Date expiryDate;

    }

    public synchronized void USSDtoSMSNumber(Context c, String ussd, int port, USSDResult USSDResult) {
        try {
            //List<Balance> list = new ArrayList<Balance>();
            Balance bal = new Balance();

            try {
                if (smsBroadcastReceiver != null)
                    c.unregisterReceiver(smsBroadcastReceiver);
            } catch (Exception e) {
                // Log.e(TAG, e.toString());
                Crashlytics.logException(e);

            }

            smsBroadcastReceiver.setListener(new SmsBroadcastReceiver.Listener() {
                @Override
                public void onTextReceived(String raw) {

                    Crashlytics.log(Log.DEBUG, TAG, "onTextReceived ...");


                    bal.balance = extractBalance(raw);
                    bal.expiryDate = extractExpiry(raw);
                    bal.rawBalance = raw;
                    if (timer != null) timer.cancel();
                    if (USSDResult != null)
                        USSDResult.onResult(bal);
                    //     if (bal.balance != null) {
                    setDialTimeComplete();

                    //    }


                }
            });

            c.registerReceiver(smsBroadcastReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));

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
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            c.startActivity(intent);

            Calendar cal = Calendar.getInstance();
            dialTime = cal.getTimeInMillis();

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    //this runs on the UI thread
                    if (timer != null) timer.cancel();
                    timer = new CountDownTimer(USSD_LIMIT * 1000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            Crashlytics.log(Log.DEBUG, TAG, "waiting for ussd " + (millisUntilFinished / 1000));

                            if (dialTimeComplete()) {
                                Crashlytics.log(Log.DEBUG, TAG, "ussd complete. Closing timer");
                                timer.cancel();
                            }
                        }

                        @Override
                        public void onFinish() {
                            Crashlytics.log(Log.DEBUG, TAG, "onFinish dials");

                            Utils.getHandlerThread().post(new Runnable() {
                                @Override
                                public void run() {

                                    USSDResult.onResult(bal);
                                }
                            });


                            timer = null;
                        }
                    }

                    ;
                    timer.start();
                }
            });
        } catch (Exception e) {
            // Log.e(TAG, e.toString());
            Crashlytics.logException(e);
            if (USSDResult != null)
                USSDResult.onResult(new Balance());

        }
    }


}