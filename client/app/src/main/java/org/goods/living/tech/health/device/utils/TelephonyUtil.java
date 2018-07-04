package org.goods.living.tech.health.device.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;

public class TelephonyUtil {


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
}