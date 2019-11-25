package com.ulproject.letscab.di.modules;

import android.content.Context;

import com.ulproject.letscab.di.qualifiers.ApplicationContextQualifier;
import com.ulproject.letscab.di.scopes.CustomApplicationScope;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationContextModule {

    private final Context context;

    public ApplicationContextModule(Context context) {
        this.context = context.getApplicationContext();
    }

    @Provides
    @CustomApplicationScope
    @ApplicationContextQualifier
    Context getContext() {
        return context;
    }
}
