package org.goods.living.tech.health.device;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import org.goods.living.tech.health.device.models.MyObjectBox;
import org.goods.living.tech.health.device.models.Stats;
import org.goods.living.tech.health.device.models.User;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.objectbox.Box;
import io.objectbox.BoxStore;

@Module
public class AppControllerModule {
    // @ContributesAndroidInjector
    //  abstract MainActivity contributeActivityInjector();


    //   @Provides
    //  static box provideNetworkApi(){
    //      return  MyObjectBox.builder().androidContext(AppController.this).build();
    //   }

    // @Inject
    //  UserService userService;
    private AppController app;

    public AppControllerModule(AppController app) {
        this.app = app;
    }


    //https://medium.com/@Zhuinden/that-missing-guide-how-to-use-dagger2-ef116fbea97
    // @Singleton
    // @Provides
    //  public UserService provideUserService(BoxStore store) {
    //       return new UserService();
    //  }

    @Provides
    @Singleton
    public AppController application() {
        return app;
    }

    @Provides
    @Inject
    SharedPreferences provideSharedPreferences(Context context) {
        return context.getSharedPreferences("locationApp", Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    BoxStore provideBoxStore(AppController app) {
        BoxStore boxStore = MyObjectBox.builder().androidContext(app).build();
        return boxStore;
    }

    @Provides
    @Singleton
    Box<Stats> provideBoxStats(BoxStore boxStore) {
        return boxStore.boxFor(Stats.class);
    }

    @Provides
    @Singleton
    Box<User> provideBoxUser(BoxStore boxStore) {
        return boxStore.boxFor(User.class);
    }

    //  @Provides
    //  @Singleton
    //  Bus provideBus(){
    //      return new Bus();
    //   }


    //  @Provides
    // @Singleton
    //  SharedPreferences provideSharedPreference(Application ctx) {
    //      return new SharedPreferences(ctx);
    //  }

    //  @Provides
    //  @Singleton
    //  DbHelper provideDbHelper(Application ctx) {
    //      return new DbHelper(ctx);
    //  }

/**
 * Returns the title for reporting about a list of {@link Location} objects.
 *
 * @param context The {@link Context}.
 *
public static String getLocationResultTitle(Context context, List<Location> locations) {
String numLocationsReported = context.getResources().getQuantityString(
R.plurals.num_locations_reported, locations.size(), locations.size());
return numLocationsReported + ": " + DateFormat.getDateTimeInstance().format(new Date());
}

 **
 * Returns te text for reporting about a list of  {@link Location} objects.
 *
 * @param locations List of {@link Location}s.
 *
private static String getLocationResultText(Context context, List<Location> locations) {
if (locations.isEmpty()) {
return context.getString(R.string.unknown_location);
}
StringBuilder sb = new StringBuilder();
for (Location location : locations) {
sb.append("(");
sb.append(location.getLatitude());
sb.append(", ");
sb.append(location.getLongitude());
sb.append(")");
sb.append("\n");
}
return sb.toString();
}

public static void setLocationUpdatesResult(Context context, List<Location> locations) {
PreferenceManager.getDefaultSharedPreferences(context)
.edit()
.putString(KEY_LOCATION_UPDATES_RESULT, getLocationResultTitle(context, locations)
+ "\n" + getLocationResultText(context, locations))
.apply();
}

public static String getLocationUpdatesResult(Context context) {
return PreferenceManager.getDefaultSharedPreferences(context)
.getString(KEY_LOCATION_UPDATES_RESULT, "");
}
 */
}
