package com.ulproject.letscab.di.component;

import com.ulproject.letscab.di.scopes.ActivityScope;
import com.ulproject.letscab.view.ui.MainActivity;

import dagger.Component;

@ActivityScope
@Component(dependencies = AppComponent.class)
public interface ActivityComponent extends AppComponent {

    void inject(MainActivity mainActivity);
}