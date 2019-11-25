package com.ulproject.letscab.application;

import android.app.Application;

import com.ulproject.letscab.di.component.AppComponent;
import com.ulproject.letscab.di.component.DaggerAppComponent;
import com.ulproject.letscab.di.modules.ApplicationContextModule;


public class MyCustomApplication extends Application {

    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        appComponent = DaggerAppComponent
                .builder()
                .applicationContextModule(new ApplicationContextModule(this))
                .build();
    }

    public AppComponent appComponent() {
        return appComponent;
    }

}

