package com.ulproject.letscab.di.component;

import com.ulproject.letscab.Repo.DriverRepo;
import com.ulproject.letscab.Repo.MarkerRepo;
import com.ulproject.letscab.di.modules.RepositoryModule;
import com.ulproject.letscab.di.modules.UtilModule;
import com.ulproject.letscab.di.scopes.CustomApplicationScope;
import com.ulproject.letscab.util.AppRxSchedulers;
import com.ulproject.letscab.util.GoogleMapHelper;
import com.ulproject.letscab.util.UiHelper;

import dagger.Component;

@CustomApplicationScope
@Component(modules = {UtilModule.class, RepositoryModule.class})
public interface AppComponent {

    UiHelper uiHelper();

    AppRxSchedulers appRxSchedulers();

    GoogleMapHelper googleMapHelper();

    DriverRepo driverRepo();

    MarkerRepo markerRepo();
}