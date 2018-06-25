package org.goods.living.tech.health.device.services;

import com.crashlytics.android.Crashlytics;

import org.goods.living.tech.health.device.models.Setting;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.objectbox.Box;

@Singleton
public class SettingService extends BaseService {

    @Inject
    Box<Setting> box;//= boxStore.boxFor(User.class);

    @Inject
    public SettingService() {
        //super(boxStore);
        //box = boxStore.boxFor(User.class);
    }

    public boolean insert(Setting model) {

        try {

            //   Box<User> userBox = boxStore.boxFor(User.class);//(AppController.getInstance()).getBoxStore().boxFor(User.class);
            box.put(model);
            return true;

        } catch (Exception e) {
            Crashlytics.logException(e);
            return false;
        }

    }

    /*
     * Only one per device
     */
    public Setting getRecord() {

        try {
            Setting model = box.query().build().findFirst();//.orderDesc(User_.createdAt).build().findFirst();

            return model;
        } catch (Exception e) {
            Crashlytics.logException(e);
            return null;
        }

    }

}
