package org.livingoods.locationApp;

import org.livingoods.locationApp.services.UserService;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;

@Singleton
@Component(modules = {AndroidInjectionModule.class, AppControllerModule.class})
public interface AppcontrollerComponent extends AndroidInjector<AppController> {

    void inject(AppController target);

    void inject(UserService target);

    void inject(MainActivity target);


}