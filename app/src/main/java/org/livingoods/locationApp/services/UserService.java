package org.livingoods.locationApp.services;

import android.util.Log;

import org.livingoods.locationApp.AppController;
import org.livingoods.locationApp.models.User;
import org.livingoods.locationApp.models.User_;

import io.objectbox.Box;

public class UserService {

    private static final String TAG = UserService.class.getSimpleName();

    //@Inject
    //@Singleton
    public void UserService() {

    }

    public boolean insertUser(User user) {

        try {

            Box<User> userBox = (AppController.getInstance()).getBoxStore().boxFor(User.class);
            userBox.put(user);
            return true;

        } catch (Exception e) {
            Log.i(TAG, e.toString());
            return false;
        }

    }

    public User getlatestRegisteredUser() {

        try {

            //if 1st run - no user record exists.
            Box<User> userBox = (AppController.getInstance()).getBoxStore().boxFor(User.class);
            User user = userBox.query().orderDesc(User_.createdAt).build().findFirst();

            return user;
        } catch (Exception e) {
            Log.i(TAG, e.toString());
            return null;
        }

    }

}
