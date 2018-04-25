package org.goods.living.tech.health.device.services;

import android.location.Location;
import android.util.Log;

import org.goods.living.tech.health.device.models.Stats;
import org.goods.living.tech.health.device.models.Stats_;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.objectbox.Box;

@Singleton
public class StatsService extends BaseService {

    @Inject
    Box<Stats> box;//= boxStore.boxFor(Stats.class);

public static long ACCURACY_THRESHHOLD =10;

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

    public @Nonnull
    List<Stats> getLatestStats() {

        try {

            //if 1st run - no user record exists.
            // Box<User> userBox = boxStore.boxFor(User.class);
            List<Stats> list = box.query().orderDesc(Stats_.createdAt).build().find(0, 15);//.orderDesc(User_.createdAt).build().findFirst();

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
     * @param locations
     * @return
     */

    public boolean insertFilteredLocationData(List<Location> locations) {
        try {

            List<Stats> list = new ArrayList<>();
            for (Location loc : locations) {

                if(loc.getAccuracy()<ACCURACY_THRESHHOLD){//take only accurate readings
                    Log.i(TAG, "skipping inaccurate readings accuracy: "+ loc.getAccuracy());
                    continue;
                }

                Stats stats = new Stats();
                stats.longitude = loc.getLongitude();
                stats.latitude = loc.getLatitude();
                stats.accuracy = loc.getAccuracy();
                stats.provider = loc.getProvider();
                stats.time = new Date(loc.getTime());
                stats.createdAt = new Date();

                list.add(stats);
            }
            box.put(list);
            return true;
        } catch (Exception e) {
            Log.i(TAG, e.toString());
            return false;
        }
    }
}
