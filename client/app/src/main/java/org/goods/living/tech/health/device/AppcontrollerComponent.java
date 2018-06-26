package org.goods.living.tech.health.device;

import org.goods.living.tech.health.device.UI.MainActivity;
import org.goods.living.tech.health.device.UI.PermissionActivity;
import org.goods.living.tech.health.device.services.JobSchedulerService;
import org.goods.living.tech.health.device.services.LocationJobService;
import org.goods.living.tech.health.device.services.LocationUpdatesIntentService;
import org.goods.living.tech.health.device.services.USSDJobService;
import org.goods.living.tech.health.device.services.USSDService;
import org.goods.living.tech.health.device.utils.LocationUpdatesBroadcastReceiver;
import org.goods.living.tech.health.device.utils.SyncAdapter;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;

@Singleton
@Component(modules = {AndroidInjectionModule.class, AppControllerModule.class})
public interface AppcontrollerComponent extends AndroidInjector<AppController> {

    void inject(AppController target);

    //  void inject(BaseService target);

    // void inject(UserService target);
    void inject(MainActivity target);

    void inject(PermissionActivity target);

    void inject(LocationUpdatesBroadcastReceiver target);

    void inject(LocationUpdatesIntentService target);

    void inject(SyncAdapter target);

    void inject(LocationJobService target);

    void inject(USSDJobService target);

    void inject(USSDService target);

    void inject(JobSchedulerService target);


}