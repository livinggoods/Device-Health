package org.goods.living.tech.health.device.services;

import android.location.Location;
import android.util.Log;

import org.goods.living.tech.health.device.models.Stats;
import org.goods.living.tech.health.device.models.Stats_;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.objectbox.Box;
import io.objectbox.query.Query;

@Singleton
public class StatsService extends BaseService {

    @Inject
    Box<Stats> box;//= boxStore.boxFor(Stats.class);

    public static long ACCURACY_THRESHHOLD = 10;
    static long CLEANUP_LIMIT = 100;

    @Inject
    public StatsService() {
        //super(boxStore);

    }

    public boolean insertStats(Stats stats) {

        try {


            box.put(stats);
            return true;

        } catch (Exception e) {
            Log.i(TAG, e.toString());
            return false;
        }

    }

    public boolean insertStats(List<Stats> stats) {

        try {


            box.put(stats);
            return true;

        } catch (Exception e) {
            Log.i(TAG, e.toString());
            return false;
        }

    }

    public @Nonnull
    List<Stats> getLatestStats(@Nullable Long limit) {

        try {

            //if 1st run - no user record exists.
            // Box<User> userBox = boxStore.boxFor(User.class);
            Query<Stats> q = box.query().orderDesc(Stats_.createdAt).build();

            List<Stats> list;

            if (limit != null) {
                list = q.find(0, limit);//find(0, 15);//.orderDesc(User_.createdAt).build().findFirst();
            } else {
                list = q.find();
            }


            return list;
        } catch (Exception e) {
            Log.i(TAG, e.toString());
            return new ArrayList<>();
        }

    }

    public @Nonnull
    List<Stats> getUnSyncedStats() {

        try {
            Query<Stats> q = box.query().equal(Stats_.synced, false).order(Stats_.createdAt).build();

            List<Stats> list = q.find();


            return list;
        } catch (Exception e) {
            Log.i(TAG, e.toString());
            return new ArrayList<>();
        }

    }

    public @Nonnull
    long getStatsCount() {

        try {

            //if 1st run - no user record exists.
            // Box<User> userBox = boxStore.boxFor(User.class);
            return box.query().build().count();

        } catch (Exception e) {
            Log.i(TAG, e.toString());
            return 0;
        }

    }

    /**
     * insert locationdata. filter out inaccurate locations
     *
     * @param locations
     * @return
     */

    public boolean insertFilteredLocationData(List<Location> locations) {
        try {

            List<Stats> list = new ArrayList<>();

            for (Location loc : locations) {

                if (loc.getAccuracy() < ACCURACY_THRESHHOLD) {//take only accurate readings
                    Log.i(TAG, "skipping inaccurate readings accuracy: " + loc.getAccuracy());
                    continue;
                }

                Stats stats = new Stats();
                stats.longitude = loc.getLongitude();
                stats.latitude = loc.getLatitude();
                stats.accuracy = Math.round(loc.getAccuracy() * 100) / 100;//2dp
                stats.provider = loc.getProvider();
                stats.recordedAt = new Date(loc.getTime());
                stats.createdAt = new Date();

                list.add(stats);
            }
            box.put(list);

            //cleanup
            deleteSyncedRecords(CLEANUP_LIMIT);

            return true;
        } catch (Exception e) {
            Log.wtf(TAG, e.toString());
            return false;
        }
    }

    public boolean deleteSyncedRecords(Long above) {
        try {


            List<Stats> list = box.query().equal(Stats_.synced, true).build().find(above, 10000);//.orderDesc(User_.createdAt).build().findFirst();
            Log.i(TAG, "Deleting synced records: " + list.size());
            box.remove(list);
            return true;
        } catch (Exception e) {
            Log.wtf(TAG, e);
            return false;
        }
    }
}
