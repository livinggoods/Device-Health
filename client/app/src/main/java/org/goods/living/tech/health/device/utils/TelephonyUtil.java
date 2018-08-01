package org.goods.living.tech.health.device.utils;

import android.content.Context;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class TelephonyUtil {

    final static String TAG = TelephonyUtil.class.getSimpleName();//BaseService.class.getSimpleName();


    private static TelephonyUtil telephonyInfo;
    public String networkSIM0;
    public String networkSIM1;
    public JSONObject telephoneDataSIM0;
    public JSONObject telephoneDataSIM1;

    Context context;

    final static String simSlotMethod[] = {
            "getDeviceIdGemini",
            "getDeviceId",
            "getDeviceIdDs",
            "getSimSerialNumberGemini",
            "getNetworkOperatorForPhone"

    };
    static String networkProvider = "getNetworkOperatorForPhone";
//    final static String simStateMethod[] = {
//            "getSimStateGemini",
//            "getSimState",
//            "getSimStates"
//
//    };


    private TelephonyUtil(Context context) {

        this.context = context;
        loadInfo();

    }

    public static TelephonyUtil getInstance(Context context) {

        if (telephonyInfo == null) {
            telephonyInfo = new TelephonyUtil(context);
        }

        return telephonyInfo;
    }

    public synchronized void loadInfo() {
        try {

            telephoneDataSIM0 = new JSONObject();
            telephoneDataSIM1 = new JSONObject();
            //   getSims(context);
            HashMap<Method, Class<?>> map = potentialTelephonyManagerMethodNamesForThisDevice(context);


            // List<String> results = new ArrayList<>();
            for (Map.Entry<Method, Class<?>> entry : map.entrySet()) {
                Method key = entry.getKey();
                Class<?> value = entry.getValue();

                String sim0 = getDeviceIdBySlot(key, 0);
                String sim1 = getDeviceIdBySlot(key, 1);

                if (sim0 != null) {

                    // results.add(key.getName() + " " + sim);
                    telephoneDataSIM0.put(key.getName(), sim0);
                    Crashlytics.log(Log.DEBUG, TAG, key.getName() + " " + sim0);

//                    if (key.getName().toLowerCase().contains("operator") && !sim1.trim().isEmpty()) {
//                        telephoneDataSIM1.put("simPresent", true);
//                    }
                    //   break;
                }
                if (sim1 != null) {

                    telephoneDataSIM1.put(key.getName(), sim1);
                    Crashlytics.log(Log.DEBUG, TAG, key.getName() + " " + sim1);

//                    if (key.getName().toLowerCase().contains("operator") && !sim1.trim().isEmpty()) {
//                        telephoneDataSIM1.put("simPresent", true);
//                    }

                }
            }

//
//            if (!telephoneDataSIM1.has("simPresent"))
//                telephoneDataSIM1 = null;
//            if (!telephoneDataSIM2.has("simPresent"))
//                telephoneDataSIM2 = null;

            //map = potentialSmsManagerMethodNamesForThisDevice(context);


            //   for (String s : results) {
            //       Crashlytics.log(Log.DEBUG, TAG, s);
            //   }


            //  for (String method : simSlotMethod) {

            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());
            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method methodLive = telephonyClass.getMethod(networkProvider, parameter);

            String sim0 = getDeviceIdBySlot(methodLive, 0);
            String sim1 = getDeviceIdBySlot(methodLive, 1);


            this.networkSIM0 = (sim0 != null && !sim0.trim().isEmpty()) ? sim0.trim() : null;
            this.networkSIM1 = (sim1 != null && !sim1.trim().isEmpty()) ? sim1.trim() : null;
            //    break;
            //   }
            //      }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String getDeviceIdBySlot(Method getSimID, int slotID) {

        String imei = null;


        try {
            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            //    Class<?> telephonyClass = Class.forName(telephony.getClass().getName());

            // Class<?>[] parameter = new Class[1];
            //    parameter[0] = int.class;
            //   Method getSimID = telephonyClass.getMethod(predictedMethodName, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimID.invoke(telephony, obParameter);

            if (ob_phone != null) {
                imei = ob_phone.toString();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imei;
    }

    Boolean getSIMStateBySlot(String predictedMethodName, int slotID) {

        Boolean isReady = null;

        try {
            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimStateGemini = telephonyClass.getMethod(predictedMethodName, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimStateGemini.invoke(telephony, obParameter);

            if (ob_phone != null) {
                int simState = Integer.parseInt(ob_phone.toString());
                if (simState == TelephonyManager.SIM_STATE_READY) {
                    isReady = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isReady;
    }


    public static HashMap<Method, Class<?>> potentialTelephonyManagerMethodNamesForThisDevice(Context context) {

        HashMap<Method, Class<?>> map = new HashMap<>();
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        Class<?> telephonyClass;
        try {
            telephonyClass = Class.forName(telephony.getClass().getName());
            Method[] methods = telephonyClass.getMethods();
            for (int idx = 0; idx < methods.length; idx++) {

                Annotation[][] parameterAnnotations = methods[idx].getParameterAnnotations();
                Class<?>[] parameterTypes = methods[idx].getParameterTypes();
                int parameterCount = parameterTypes.length;
                // for (int i = 0; i < parameterCount; i++) {
                // Annotation[] annotations = parameterAnnotations[i];
                //      Class<?> parameterClazz = parameterTypes[i];
                //   }
                if (methods[idx].getName().startsWith("get") && parameterCount == 1) {
                    Class<?> parameterClazz = parameterTypes[0];

                    if (parameterClazz.isPrimitive()) {//Number.class.isAssignableFrom(parameterClazz.getDeclaringClass())) {//parameterClazz.isPrimitive() ||
                        //if (methods[idx].getParameterTypes().length == 1 && methods[idx].getParameterAnnotations()[0].length == 1 && (methods[idx].getReturnType().isAssignableFrom(String.class) || methods[idx].getReturnType().isAssignableFrom(Boolean.class)))
                        System.out.println("\n" + methods[idx].getName());//+ " declared by " + methods[idx].getDeclaringClass()
                        map.put(methods[idx], parameterClazz);
                    }
                }
                //   if (methods[idx].getName().contains("Sms")) {
                // Class<?> parameterClazz = parameterTypes[0];
                System.out.println("\n" + methods[idx].getName());//+ " declared by " + methods[idx].getDeclaringClass()
                //  System.out.println("\n" + parameterTypes);//+ " declared by " + methods[idx].getDeclaringClass()
                //   }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static HashMap<Method, Class<?>> potentialSmsManagerMethodNamesForThisDevice(Context context) {

        HashMap<Method, Class<?>> map = new HashMap<>();
        //  TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        Class<?> telephonyClass;
        try {
            telephonyClass = Class.forName(SmsManager.class.getName());
            Method[] methods = telephonyClass.getMethods();

            Method method = Class.forName("android.os.ServiceManager").getMethod("listServices");
            method.setAccessible(true);
            Object param = method.invoke(null);


            for (int idx = 0; idx < methods.length; idx++) {

                Annotation[][] parameterAnnotations = methods[idx].getParameterAnnotations();
                Class<?>[] parameterTypes = methods[idx].getParameterTypes();
                int parameterCount = parameterTypes.length;
                // for (int i = 0; i < parameterCount; i++) {
                // Annotation[] annotations = parameterAnnotations[i];
                //      Class<?> parameterClazz = parameterTypes[i];
                //   }
                if (methods[idx].getName().startsWith("get") && parameterCount == 1) {
                    Class<?> parameterClazz = parameterTypes[0];
                    if (parameterClazz.isPrimitive()) {//parameterClazz.isPrimitive() ||
                        //if (methods[idx].getParameterTypes().length == 1 && methods[idx].getParameterAnnotations()[0].length == 1 && (methods[idx].getReturnType().isAssignableFrom(String.class) || methods[idx].getReturnType().isAssignableFrom(Boolean.class)))
                        System.out.println("\n" + methods[idx].getName());//+ " declared by " + methods[idx].getDeclaringClass()
                        map.put(methods[idx], parameterClazz);
                    }
                }
                //   if (methods[idx].getName().contains("Sms")) {
                // Class<?> parameterClazz = parameterTypes[0];
                System.out.println("\n" + methods[idx].getName());//+ " declared by " + methods[idx].getDeclaringClass()
                //  System.out.println("\n" + parameterTypes);//+ " declared by " + methods[idx].getDeclaringClass()
                //   }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

}