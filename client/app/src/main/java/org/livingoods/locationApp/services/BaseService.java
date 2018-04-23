package org.livingoods.locationApp.services;

import javax.inject.Inject;

import io.objectbox.BoxStore;

public class BaseService {

    final String TAG = this.getClass().getSimpleName();//BaseService.class.getSimpleName();

    @Inject
    BoxStore boxStore;

    //@Inject
    //@Singleton
    public BaseService() {//BoxStore boxStore) {
        // this.boxStore = boxStore;
        //  AppController.getInstance().getComponent()
        //          .inject(this);
    }


}
