package com.ulproject.letscab.di.modules;

import android.content.Context;

import com.ulproject.letscab.di.qualifiers.ApplicationContextQualifier;
import com.ulproject.letscab.di.scopes.CustomApplicationScope;
import com.ulproject.letscab.util.AppRxSchedulers;
import com.ulproject.letscab.util.GoogleMapHelper;
import com.ulproject.letscab.util.UiHelper;

import dagger.Module;
import dagger.Provides;

@Module(includes = {ApplicationContextModule.class})
public class UtilModule {

    @Provides
    @CustomApplicationScope
    public UiHelper uiHelper(@ApplicationContextQualifier Context context) {
        return new UiHelper(context);
    }

    @Provides
    @CustomApplicationScope
    public AppRxSchedulers appRxSchedulers() {
        return new AppRxSchedulers();
    }

    @Provides
    @CustomApplicationScope
    public GoogleMapHelper googleMapHelper(@ApplicationContextQualifier Context context) {
        return new GoogleMapHelper(context.getResources());
    }
}