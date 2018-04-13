package org.livingoods.locationApp;

import android.app.Application;

import io.objectbox.BoxStore;

public class AppController extends Application {

    // Create the instance
    private static AppController instance;

    private AppcontrollerComponent component;
    private BoxStore boxStore;

    public static AppController getInstance() {
        if (instance == null) {
            synchronized (AppController.class) {
                if (instance == null)
                    instance = new AppController();
            }
        }
        // Return the instance
        return instance;
    }


    public AppcontrollerComponent getComponent() {
        return component;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerAppcontrollerComponent.builder().appControllerModule(new AppControllerModule(this)).build();
        component.inject(this);
        if (instance == null) {
            instance = this;
        }
        // component = DaggerAppcontrollerComponent.builder().appModule(new AppModule(this)).build();

        //  DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,"users-db"); //The users-db here is the name of our database.
        //  Database db = helper.getWritableDb();
        //   daoSession = new DaoMaster(db).newSession();

        ///// Using the below lines of code we can toggle ENCRYPTED to true or false in other to use either an encrypted database or not.
//      DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, ENCRYPTED ? "users-db-encrypted" : "users-db");
//      Database db = ENCRYPTED ? helper.getEncryptedWritableDb("super-secret") : helper.getWritableDb();
//      daoSession = new DaoMaster(db).newSession();

// do this once, for example in your Application class
        //       boxStore = MyObjectBox.builder().androidContext(AppController.this).build();
// do this in your activities/fragments to get hold of a Box
        // notesBox = ((AppController) getApplication()).getBoxStore().boxFor(Note.class);
    }

    public BoxStore getBoxStore() {
        return boxStore;
    }
}
