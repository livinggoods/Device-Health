package org.goods.living.tech.health.device.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelephonyUtil {

    final static String TAG = TelephonyUtil.class.getSimpleName();//BaseService.class.getSimpleName();


    private static TelephonyUtil telephonyInfo;
    public String networkSIM1;
    public String networkSIM2;
    public JSONObject telephoneDataSIM1;
    public JSONObject telephoneDataSIM2;

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
        telephoneDataSIM1 = new JSONObject();
        telephoneDataSIM2 = new JSONObject();
        loadInfo();

    }

    public static TelephonyUtil getInstance(Context context) {

        if (telephonyInfo == null) {
            telephonyInfo = new TelephonyUtil(context);
        }

        return telephonyInfo;
    }

    public void loadInfo() {
        try {


            //   getSims(context);
            HashMap<Method, Class<?>> map = potentialTelephonyManagerMethodNamesForThisDevice(context);
            // List<String> results = new ArrayList<>();
            for (Map.Entry<Method, Class<?>> entry : map.entrySet()) {
                Method key = entry.getKey();
                Class<?> value = entry.getValue();

                String sim1 = getDeviceIdBySlot(key, 0);
                String sim2 = getDeviceIdBySlot(key, 1);

                if (sim1 != null) {
                    // results.add(key.getName() + " " + sim);
                    telephoneDataSIM1.put(key.getName(), sim1);
                    telephoneDataSIM2.put(key.getName(), sim2);
                    Crashlytics.log(Log.DEBUG, TAG, key.getName() + " " + sim1);
                    //   break;
                }
            }


            //   for (String s : results) {
            //       Crashlytics.log(Log.DEBUG, TAG, s);
            //   }


            //  for (String method : simSlotMethod) {

            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());
            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method methodLive = telephonyClass.getMethod(networkProvider, parameter);

            String sim1 = getDeviceIdBySlot(methodLive, 0);
            String sim2 = getDeviceIdBySlot(methodLive, 1);


            this.networkSIM1 = (sim1 != null && !sim1.trim().isEmpty()) ? sim1.trim() : null;
            this.networkSIM2 = (sim2 != null && !sim2.trim().isEmpty()) ? sim2.trim() : null;
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
                    if (parameterClazz.isPrimitive() || Number.class.isAssignableFrom(parameterClazz)) {
                        //if (methods[idx].getParameterTypes().length == 1 && methods[idx].getParameterAnnotations()[0].length == 1 && (methods[idx].getReturnType().isAssignableFrom(String.class) || methods[idx].getReturnType().isAssignableFrom(Boolean.class)))
                        System.out.println("\n" + methods[idx].getName());//+ " declared by " + methods[idx].getDeclaringClass()
                        map.put(methods[idx], parameterClazz);
                    }
                }


            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static String getSimSerial(Context c) {
        String s = null;
        TelephonyManager telephonyManager = ((TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE));

        if (android.os.Build.VERSION.SDK_INT > 22) { /*Ask Dungerous Permissions here*/
            if (c.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                s = telephonyManager.getSimSerialNumber();
            }
        } else {
            s = telephonyManager.getSimSerialNumber();
        }
        return s;
    }

    public static void getSims(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

// Get information about all radio modules on device board
// and check what you need by calling #getCellIdentity.

        List<CellInfo> allCellInfo = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= 23) { /*Ask Dungerous Permissions here*/
            if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                allCellInfo = manager.getAllCellInfo();
            }
        }


        for (CellInfo cellInfo : allCellInfo) {
            if (cellInfo instanceof CellInfoGsm) {
                CellIdentityGsm cellIdentity = ((CellInfoGsm) cellInfo).getCellIdentity();
                //TODO Use cellIdentity to check MCC/MNC code, for instance.
            } else if (cellInfo instanceof CellInfoWcdma) {
                CellIdentityWcdma cellIdentity = ((CellInfoWcdma) cellInfo).getCellIdentity();
            } else if (cellInfo instanceof CellInfoLte) {
                CellIdentityLte cellIdentity = ((CellInfoLte) cellInfo).getCellIdentity();
            } else if (cellInfo instanceof CellInfoCdma) {
                CellIdentityCdma cellIdentity = ((CellInfoCdma) cellInfo).getCellIdentity();
            }
        }
    }

}