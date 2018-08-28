package org.goods.living.tech.health.device.utils;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.services.SyncService;

import javax.inject.Inject;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    ContentResolver mContentResolver;
    Context mContext;

    @Inject
    SyncService syncService;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mContentResolver = context.getContentResolver();
        // myPrefs = new MyPrefs(mContext);
        //  apiInterface = ServiceGenerator.createService(ApiInterface.class);
        AppController appController;
        if (!(context.getApplicationContext() instanceof AppController)) {
            appController = ((AppController) context.getApplicationContext());

        } else {
            appController = AppController.getInstance();

        }
        appController.getComponent().inject(this);

    }

    /**
     * Manual force Android to perform a sync with our SyncAdapter.
     */
    public static void performSync() {
        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);


        ContentResolver.requestSync(AuthenticatorService.getAccount(),
                AuthenticatorService.CONTENT_AUTHORITY, b);
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.e("inPerformSync", "syncing");
        // sync data or perform your action


        Utils.getHandlerThread().post(new Runnable() {
            @Override
            public void run() {
                syncService.sync(mContext);

                AppController appController;
                if (!(mContext.getApplicationContext() instanceof AppController)) {
                    appController = ((AppController) mContext.getApplicationContext());

                } else {
                    appController = AppController.getInstance();

                }


            }
        });


    }

}

