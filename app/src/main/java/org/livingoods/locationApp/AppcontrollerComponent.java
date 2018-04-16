package org.livingoods.locationApp;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;

@Singleton
@Component(modules = {AndroidInjectionModule.class, AppControllerModule.class})
public interface AppcontrollerComponent extends AndroidInjector<AppController> {

    void inject(AppController target);

    // void inject(BaseService target);

    // void inject(UserService target);
    void inject(MainActivity target);


}