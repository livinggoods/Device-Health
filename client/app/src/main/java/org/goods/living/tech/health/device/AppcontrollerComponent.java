package org.goods.living.tech.health.device;

import org.goods.living.tech.health.device.UI.MainActivity;
import org.goods.living.tech.health.device.UI.PermissionActivity;
import org.goods.living.tech.health.device.UI.RegisterUserFragment;
import org.goods.living.tech.health.device.receivers.LocationUpdatesBroadcastReceiver;
import org.goods.living.tech.health.device.receivers.SmsBroadcastReceiver;
import org.goods.living.tech.health.device.receivers.USSDBalanceBroadcastReceiver;
import org.goods.living.tech.health.device.receivers.UnlockBroadcastReceiver;
import org.goods.living.tech.health.device.receivers.UpdateBroadcastReceiver;
import org.goods.living.tech.health.device.services.ActivityUpdatesService;
import org.goods.living.tech.health.device.services.FirebaseMessageService;
import org.goods.living.tech.health.device.services.JobSchedulerService;
import org.goods.living.tech.health.device.services.LocationJobService;
import org.goods.living.tech.health.device.services.LocationUpdatesIntentService;
import org.goods.living.tech.health.device.services.RegistrationService;
import org.goods.living.tech.health.device.services.USSDService;
import org.goods.living.tech.health.device.utils.DataBalanceHelper;
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

    void inject(USSDService target);

    void inject(JobSchedulerService target);

    void inject(RegistrationService target);

    void inject(RegisterUserFragment target);

    void inject(DataBalanceHelper target);

    void inject(SmsBroadcastReceiver target);

    void inject(USSDBalanceBroadcastReceiver target);

    void inject(UpdateBroadcastReceiver target);

    void inject(FirebaseMessageService target);

    void inject(ActivityUpdatesService target);

    void inject(UnlockBroadcastReceiver target);

}