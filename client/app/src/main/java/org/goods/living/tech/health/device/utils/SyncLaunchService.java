package org.goods.living.tech.health.device.utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SyncLaunchService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapter sSyncAdapter = null;


    @Override
    public void onCreate() {
        Log.e("SyncLaunchService", "Service created");
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                Log.e("SyncLaunchService", "sSyncAdapter");
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("SyncService", "Service destroyed");
    }
}