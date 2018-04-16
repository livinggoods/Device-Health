package org.livingoods.locationApp;

import android.content.Context;
import android.content.SharedPreferences;

import org.livingoods.locationApp.models.MyObjectBox;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
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
}
