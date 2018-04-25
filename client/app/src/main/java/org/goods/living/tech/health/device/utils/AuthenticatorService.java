package org.goods.living.tech.health.device.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class AuthenticatorService extends Service {

    public static final String ACCOUNT = "LG locationApp";
    public static final long SYNC_FREQUENCY = 60 * 60; // 1 hour (seconds)
    static final String ACCOUNT_TYPE = "org.goods.living.tech.health.device";
    static String PREF_SETUP_COMPLETE = "PREF_SETUP_COMPLETE";
    // Instance field that stores the authenticator object
    private Authenticator mAuthenticator;

    public static Account getAccount() {//Context context
        // Note: Normally the account name is set to the user's identity (username or email
        // address). However, since we aren't actually using any user accounts, it makes more sense
        // to use a generic string in this case.
        //
        // This string should *not* be localized. If the user switches locale, we would not be
        // able to locate the old account, and may erroneously register multiple accounts.
        //  final String accountName = ACCOUNT;
        // return new Account(context.getString(R.string.app_name), ACCOUNT_TYPE);// context.getPackageName());
        return new Account(ACCOUNT, ACCOUNT_TYPE);
    }

    public static void setSyncInterval(long updateSeconds) {

        Account account = getAccount();

        ContentResolver.addPeriodicSync(account, Utils.CONTENT_AUTHORITY, new Bundle(), updateSeconds);
    }

    public static void createSyncAccount(Context c) {
        // Flag to determine if this is a new account or not
        boolean created = false;

        // Get an account and the account manager
        Account account = getAccount();
        AccountManager manager = (AccountManager) c.getSystemService(Context.ACCOUNT_SERVICE);

        // Attempt to explicitly create the account with no password or extra data
        if (manager.addAccountExplicitly(account, null, null)) {


            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(account, Utils.CONTENT_AUTHORITY, 1);
            // Inform the system that this account is eligible for auto sync when the network is up
            ContentResolver.setSyncAutomatically(account, Utils.CONTENT_AUTHORITY, true);
            //  ContentResolver.setMasterSyncAutomatically(true);

            // Recommend a schedule for automatic synchronization. The system may modify this based
            // on other scheduled syncs and network utilization.
            // ContentResolver.addPeriodicSync(account, CONTENT_AUTHORITY, new Bundle(), SYNC_FREQUENCY);
            setSyncInterval(SYNC_FREQUENCY);

            created = true;
        }

        // Force a sync if the account was just created
        if (created) {
            SyncAdapter.performSync();
        }
    }

    @Override
    public void onCreate() {
        Log.i("APP", "Service created");
        // Create a new authenticator object
        mAuthenticator = new Authenticator(this);
    }

    /**
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

    @Override
    public void onDestroy() {
        Log.i("APP", "Service destroyed");
    }

}