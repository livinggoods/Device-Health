package org.goods.living.tech.health.device.services;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.goods.living.tech.health.device.models.DataBalance;
import org.goods.living.tech.health.device.models.DataBalance_;

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
public class DataBalanceService extends BaseService {

    @Inject
    Box<DataBalance> box;//= boxStore.boxFor(Stats.class);

    DataBalanceService.USSDListener listener;

    @Inject
    public DataBalanceService() {
        //super(boxStore);

    }

    public boolean insertDataBalance(DataBalance model) {

        try {


            box.put(model);
            return true;

        } catch (Exception e) {
            Crashlytics.log(Log.DEBUG, TAG, e.toString());
            return false;
        }

    }

    public boolean insertDataBalance(List<DataBalance> models) {

        try {


            box.put(models);
            return true;

        } catch (Exception e) {
            Crashlytics.logException(e);
            return false;
        }

    }

    public @Nonnull
    List<DataBalance> getLatestDataBalance(@Nullable Long limit) {

        try {

            //if 1st run - no user record exists.
            // Box<User> userBox = boxStore.boxFor(User.class);
            Query<DataBalance> q = box.query().orderDesc(DataBalance_.createdAt).build();

            List<DataBalance> list;

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
    List<DataBalance> getUnSyncedRecords(long limit) {//limit for pagination

        try {
            return getUnSyncedFilteredRecords(limit);
        } catch (Exception e) {
            Crashlytics.logException(e);
            return new ArrayList<>();
        }

    }

    public @Nonnull
    List<DataBalance> getUnSyncedFilteredRecords(long limit) {//limit for pagination

        try {
            Query<DataBalance> q = box.query().equal(DataBalance_.synced, false).order(DataBalance_.createdAt).build();

            List<DataBalance> list = q.find(0, limit);
            return list;
        } catch (Exception e) {
            Crashlytics.logException(e);
            return new ArrayList<>();
        }

    }

    public @Nonnull
    long getDataBalanceCount() {

        try {

            //if 1st run - no user record exists.
            // Box<User> userBox = boxStore.boxFor(User.class);
            return box.query().build().count();

        } catch (Exception e) {
            Crashlytics.logException(e);
            return 0;
        }

    }

    public boolean insert(Double bal, String raw) {
        try {

            DataBalance model = new DataBalance();
            model.balance = bal;
            model.balanceMessage = raw;
            model.recordedAt = new Date();
            model.createdAt = model.recordedAt;

            box.put(model);


            return true;
        } catch (Exception e) {
            Crashlytics.logException(e);
            return false;
        }
    }

    public boolean insertRecords(DataBalance record) {

        try {


            box.put(record);
            return true;

        } catch (Exception e) {
            Crashlytics.log(Log.DEBUG, TAG, e.toString());
            return false;
        }

    }

    public boolean insertRecords(List<DataBalance> records) {

        try {
            box.put(records);
            return true;

        } catch (Exception e) {
            Crashlytics.logException(e);
            return false;
        }

    }

    public boolean deleteSyncedRecordsOlder(Long id) {
        try {


            List<DataBalance> list = box.query().less(DataBalance_.id, id).build().find();//find(above, 10000);//.orderDesc(User_.createdAt).build().findFirst();
            Crashlytics.log(Log.DEBUG, TAG, "Deleting synced records: " + list.size());
            box.remove(list);
            return true;
        } catch (Exception e) {
            Crashlytics.logException(e);
            return false;
        }
    }

    public boolean deleteSyncedRecords(List<DataBalance> list) {
        try {

            Crashlytics.log(Log.DEBUG, TAG, "Deleting synced records: " + list.size());
            box.remove(list);

            List<DataBalance> l = box.query().equal(DataBalance_.synced, true).
                    build().find();//.orderDesc(User_.createdAt).build().findFirst();
            box.remove(list);


            //remove older records since should have synced.
            if (list.size() > 0) {
                DataBalance s = list.get(list.size() - 1);
                list = box.query().less(DataBalance_.id, s.id).
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

    public long countRecords() {
        try {

            return box.query().build().count();//.orderDesc(User_.createdAt).build().findFirst();

        } catch (Exception e) {
            Crashlytics.logException(e);
            return 0l;
        }
    }

    public @Nonnull
    List<DataBalance> getLatestRecords(@Nullable Long limit) {

        try {

            //if 1st run - no user record exists.
            // Box<User> userBox = boxStore.boxFor(User.class);
            Query<DataBalance> q = box.query().orderDesc(DataBalance_.createdAt).build();

            List<DataBalance> list;

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

    public interface USSDListener {
        void onUSSDReceived(String balance, String raw);
    }

    public void bindListener(USSDListener list) {
        listener = list;
    }

    public void unbindListener() {
        listener = null;
    }

    public USSDListener getListener() {
        return listener;
    }
}
