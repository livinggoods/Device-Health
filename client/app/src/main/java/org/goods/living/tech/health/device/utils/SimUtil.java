package org.goods.living.tech.health.device.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;

public class SimUtil {

    final static String TAG = SimUtil.class.getSimpleName();//BaseService.class.getSimpleName();

    Context context;

    final static String simSlotMethod[] = {
            "getDeviceIdGemini",
            "getDeviceId",
            "getDeviceIdDs",
            "getSimSerialNumberGemini",
            "getNetworkOperatorForPhone"

    };

    public static boolean sendSMS(Context ctx, int simID, String toNum, String centerNum, String smsText, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        String name;


        try {


            SmsManager smsManager = null;
            // Sim One
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                smsManager = SmsManager.getSmsManagerForSubscriptionId(simID);

            }
            smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(toNum, null, smsText, null, null);

            return true;

//            if (simID == 0) {
//                name = "isms";
//                // for model : "Philips T939" name = "isms0"
//            } else if (simID == 1) {
//                name = "isms2";
//            } else {
//                throw new Exception("can not get service which for sim '" + simID + "', only 0,1 accepted as values");
//            }
//            Method method = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
//            method.setAccessible(true);
//            Object param = method.invoke(null, name);
//
//            method = Class.forName("com.android.internal.telephony.ISms$Stub").getDeclaredMethod("asInterface", IBinder.class);
//            method.setAccessible(true);
//
//
//            Object stubObj = method.invoke(null, param);
//            //    if (Build.VERSION.SDK_INT < 18) {
////                Method[] methods = stubObj.getClass().getMethods();
////
////                for (int idx = 0; idx < methods.length; idx++) {
////                    System.out.println("\n" + methods[idx].getName());
////                }
//
//            //      method = stubObj.getClass().getMethod("sendText", String.class, String.class, String.class, PendingIntent.class, PendingIntent.class);
//            //      method.invoke(stubObj, toNum, centerNum, smsText, sentIntent, deliveryIntent);
//            //   } else {
//
//            Method[] methods = stubObj.getClass().getMethods();
//
//            for (int idx = 0; idx < methods.length; idx++) {
//                System.out.println("\n" + methods[idx].getName());
//
//                if ("sendTextForSubscriber".equals(methods[idx].getName())) {
//                    methods[idx].setAccessible(true);
//                    Class<?>[] parameterTypes = methods[idx].getParameterTypes();
//                    int parameterCount = parameterTypes.length;
//                    for (Class p : methods[idx].getParameterTypes()) {
//                        System.err.println("  " + p.getName());
//                    }
//
//                    if (parameterTypes.length == 7) {
//                        //  method = stubObj.getClass().getMethod("sendTextForSubscriber", Integer.class, String.class, String.class, String.class, String.class, PendingIntent.class, PendingIntent.class, Boolean.class);
//                        methods[idx].invoke(stubObj, 0, ctx.getPackageName(), toNum, centerNum, smsText, sentIntent, deliveryIntent);
//
//                        //    methods[idx].invoke(stubObj, 1, ctx.getPackageName(), toNum, centerNum, smsText, sentIntent, deliveryIntent);
//
//                        return true;
//
//                    }
//
//                    if (parameterTypes.length == 8) {
//                        //  method = stubObj.getClass().getMethod("sendTextForSubscriber", Integer.class, String.class, String.class, String.class, String.class, PendingIntent.class, PendingIntent.class, Boolean.class);
//                        methods[idx].invoke(stubObj, 0, ctx.getPackageName(), toNum, centerNum, smsText, sentIntent, deliveryIntent, true);
//                        return true;
//
//                    }
//                }
//
//            }
//
//            // method = stubObj.getClass().getMethod("sendText", String.class, String.class, String.class, String.class, PendingIntent.class, PendingIntent.class);
//            // method.invoke(stubObj, ctx.getPackageName(), toNum, centerNum, smsText, sentIntent, deliveryIntent);
//            //  method = stubObj.getClass().getMethod("sendTextForSubscriber", Integer.class, String.class, String.class, String.class, String.class, PendingIntent.class, PendingIntent.class, Boolean.class);
//
//
//            //    }
//
//            return true;
        } catch (Exception e) {
            Log.e(TAG, "Exception:" + e.getMessage());
            return false;
        }

    }


}
