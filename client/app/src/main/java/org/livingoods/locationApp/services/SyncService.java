package org.livingoods.locationApp.services;

import android.util.Log;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SyncService extends BaseService {


    @Inject
    StatsService statsService;

    @Inject
    UserService userService;

    @Inject
    public SyncService() {
        //super(boxStore);

    }

    public void sync() {

        try {

            syncUser();
            syncStats();
        } catch (Exception e) {
            Log.e(TAG, e.toString());

        }

    }

    public void syncUser() {

        try {

        } catch (Exception e) {
            Log.e(TAG, e.toString());

        }
    }

    public void syncStats() {

        try {

        } catch (Exception e) {
            Log.e(TAG, e.toString());

        }
    }
}
