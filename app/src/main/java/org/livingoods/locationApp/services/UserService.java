package org.livingoods.locationApp.services;

import android.util.Log;

import org.livingoods.locationApp.models.User;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.objectbox.Box;

@Singleton
public class UserService extends BaseService {

    @Inject
    public UserService() {
        //super(boxStore);

    }

    public boolean insertUser(User user) {

        try {

            Box<User> userBox = boxStore.boxFor(User.class);//(AppController.getInstance()).getBoxStore().boxFor(User.class);
            userBox.put(user);
            return true;

        } catch (Exception e) {
            Log.i(TAG, e.toString());
            return false;
        }

    }

    /*
     * Only one user per device
     */
    public User getRegisteredUser() {

        try {

            //if 1st run - no user record exists.
            Box<User> userBox = boxStore.boxFor(User.class);
            User user = userBox.query().build().findFirst();//.orderDesc(User_.createdAt).build().findFirst();

            return user;
        } catch (Exception e) {
            Log.i(TAG, e.toString());
            return null;
        }

    }

}
