package org.goods.living.tech.health.device.services;

import android.location.Location;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.goods.living.tech.health.device.models.Stats;
import org.goods.living.tech.health.device.models.Stats_;
import org.goods.living.tech.health.device.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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

    // public static long ACCURACY_THRESHHOLD = 10;

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    @Inject
    public StatsService() {
        //super(boxStore);

    }

    public boolean insertRecords(Stats record) {

        try {


            box.put(record);
            return true;

        } catch (Exception e) {
            Crashlytics.log(Log.DEBUG, TAG, e.toString());
            return false;
        }

    }

    public boolean insertRecords(List<Stats> records) {

        try {


            box.put(records);
            return true;

        } catch (Exception e) {
            Crashlytics.logException(e);
            return false;
        }

    }

    public @Nonnull
    List<Stats> getLatestRecords(@Nullable Long limit) {

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
            Crashlytics.logException(e);
            return new ArrayList<>();
        }

    }

    public @Nonnull
    List<Stats> getUnSyncedRecords(long limit) {//limit for pagination

        try {
            return getUnSyncedFilteredRecords(limit);
        } catch (Exception e) {
            Crashlytics.logException(e);
            return new ArrayList<>();
        }

    }

    public @Nonnull
    List<Stats> getUnSyncedFilteredRecords(long limit) {//limit for pagination

        try {
            Query<Stats> q = box.query().equal(Stats_.synced, false).order(Stats_.createdAt).build();

            List<Stats> list = q.find(0, limit);
            HashMap<String, List<Stats>> hashMap = new HashMap<String, List<Stats>>();

            //group by location
            for (Stats s : list) {
                String loc = s.latitude + "" + s.longitude;
                if (!hashMap.containsKey(loc)) {
                    List<Stats> l = new ArrayList<Stats>();
                    l.add(s);

                    hashMap.put(loc, l);
                } else {
                    hashMap.get(loc).add(s);
                }
            }

            List<Stats> trimmedList = new ArrayList<Stats>();

            //compress to first and last
            for (Map.Entry<String, List<Stats>> set : hashMap.entrySet()) {
                trimmedList.add(set.getValue().get(0));
                if (set.getValue().size() > 1) {
                    trimmedList.add(set.getValue().get(set.getValue().size() - 1));//get last
                }

            }


            return trimmedList;
        } catch (Exception e) {
            Crashlytics.logException(e);
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
            Crashlytics.logException(e);
            return 0;
        }

    }

    /**
     * insert locationdata. filter out inaccurate locations
     *
     * @param locations
     * @return
     */

    public boolean insertLocationData(List<Location> locations, Double brightness, Integer batteryLevel) {
        try {

            List<Stats> list = new ArrayList<>();

            for (Location loc : locations) {
//
//                if (loc.getAccuracy() < ACCURACY_THRESHHOLD) {//take only accurate readings
//                    Crashlytics.log(Log.DEBUG, TAG, String.format("skipping inaccurate readings accuracy: %s  lat: %s  lon: %s ", loc.getAccuracy(), loc.getLatitude(), loc.getLongitude()));
//                    continue;
//                }

                Stats stats = new Stats();
                stats.longitude = loc.getLongitude();
                stats.latitude = loc.getLatitude();
                stats.accuracy = Math.round(loc.getAccuracy() * 100) / 100;//2dp
                stats.provider = loc.getProvider();
                stats.recordedAt = new Date(loc.getTime());


                stats.batteryLevel = batteryLevel;
                stats.brightness = brightness;
                stats.createdAt = new Date();


                String formattedDate = Utils.getStringTimeStampWithTimezoneFromDate(stats.recordedAt, TimeZone.getTimeZone(Utils.TIMEZONE_UTC));
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss Z");
                Date serverRecordedAt = dateFormat.parse(formattedDate);

                list.add(stats);
                //filter out records withing time threshhold:
//                if (prevRecordedAt != null) {
//                    Calendar c = Calendar.getInstance();
//                    c.setTime(prevRecordedAt);
//                    c.add(Calendar.MINUTE, 5);
//                    if (!serverRecordedAt.after(c.getTime())) {
//
//                        //if more accurate consider it
//                        if (prevAccuracy > stats.accuracy) {
//                            prevAccuracy = stats.accuracy;
//                            prevRecordedAt = serverRecordedAt;
//                            list.add(stats);
//                        }
//
//                        continue;
//                    } else {
//                        prevRecordedAt = serverRecordedAt;
//                        prevAccuracy = stats.accuracy;
//                        list.add(stats);
//                    }
//                }


            }
            box.put(list);


            return true;
        } catch (Exception e) {
            Crashlytics.logException(e);
            return false;
        }
    }

    public boolean insertLocation(Location location, Double brightness, Integer batteryLevel) {
        try {


            Stats stats = new Stats();
            stats.longitude = location.getLongitude();
            stats.latitude = location.getLatitude();
            stats.accuracy = Math.round(location.getAccuracy() * 100) / 100;//2dp
            stats.provider = location.getProvider();
            stats.recordedAt = new Date(location.getTime());

            stats.brightness = brightness;
            stats.batteryLevel = batteryLevel;

            stats.createdAt = new Date();

            box.put(stats);


            return true;
        } catch (Exception e) {
            Crashlytics.logException(e);
            return false;
        }
    }

    public boolean insertMessageData(String reason) {
        try {

            Stats stats = new Stats();
            stats.message = reason;
            stats.recordedAt = new Date();
            stats.createdAt = stats.recordedAt;


            box.put(stats);

            return true;
        } catch (Exception e) {
            Crashlytics.logException(e);
            return false;
        }
    }

    public boolean deleteSyncedRecordsOlder(Long id) {
        try {


            List<Stats> list = box.query().less(Stats_.id, id).build().find();//find(above, 10000);//.orderDesc(User_.createdAt).build().findFirst();
            Crashlytics.log(Log.DEBUG, TAG, "Deleting synced records: " + list.size());
            box.remove(list);
            return true;
        } catch (Exception e) {
            Crashlytics.logException(e);
            return false;
        }
    }

    public boolean deleteSyncedRecords(List<Stats> list) {
        try {


            //     List<Stats> list = box.query().equal(Stats_.synced, true).less(Stats_.id, latestId).
            //             build().find();//.orderDesc(User_.createdAt).build().findFirst();
            Crashlytics.log(Log.DEBUG, TAG, "Deleting synced records: " + list.size());
            box.remove(list);

            List<Stats> l = box.query().equal(Stats_.synced, true).
                    build().find();//.orderDesc(User_.createdAt).build().findFirst();
            box.remove(list);


            //remove older records since should have synced.
            if (list.size() > 0) {
                Stats s = list.get(list.size() - 1);
                list = box.query().less(Stats_.id, s.id).
                        build().find();//.orderDesc(User_.createdAt).build().findFirst();
                Crashlytics.log(Log.DEBUG, TAG, "Deleting older sync records under : " + s.id);
                box.remove(list);
            }

            return true;
        } catch (Exception e) {
            Crashlytics.logException(e);
            return false;
        }
    }

    public long countSyncedRecords() {
        try {

            return box.query().equal(Stats_.synced, true).build().count();//.orderDesc(User_.createdAt).build().findFirst();

        } catch (Exception e) {
            Crashlytics.logException(e);
            return 0l;
        }
    }

    public long countRecords() {
        try {

            return box.query().build().count();//.orderDesc(User_.createdAt).build().findFirst();

        } catch (Exception e) {
            Crashlytics.logException(e);
            return 0l;
        }
    }


    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

}
