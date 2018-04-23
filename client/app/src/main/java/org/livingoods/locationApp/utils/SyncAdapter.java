package org.livingoods.locationApp.utils;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import org.livingoods.locationApp.AppController;
import org.livingoods.locationApp.services.SyncService;

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

        AppController.getInstance().getComponent().inject(this);

    }

    /**
     * Manual force Android to perform a sync with our SyncAdapter.
     */
    public static void performSync() {
        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        ContentResolver.setSyncAutomatically(AuthenticatorService.getAccount(), Utils.CONTENT_AUTHORITY, true);

        ContentResolver.requestSync(AuthenticatorService.getAccount(),
                Utils.CONTENT_AUTHORITY, b);
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.e("inPerformSync", "syncing");
        // sync data or perform your action

        syncService.sync();

    }

}

