package com.ulproject.letscab.di.modules;

import com.ulproject.letscab.Repo.DriverRepo;
import com.ulproject.letscab.Repo.MarkerRepo;
import com.ulproject.letscab.collections.DriverCollection;
import com.ulproject.letscab.collections.MarkerCollection;
import com.ulproject.letscab.di.scopes.CustomApplicationScope;
import com.ulproject.letscab.util.AppRxSchedulers;

import dagger.Module;
import dagger.Provides;

@Module(includes = {UtilModule.class})
public class RepositoryModule {

    @Provides
    @CustomApplicationScope
    public DriverCollection driverCollection(AppRxSchedulers appRxSchedulers) {
        return new DriverCollection(appRxSchedulers);
    }

    @Provides
    @CustomApplicationScope
    public MarkerCollection markerCollection() {
        return new MarkerCollection();
    }

    @Provides
    @CustomApplicationScope
    public DriverRepo driverRepo(DriverCollection driverCollection) {
        return new DriverRepo(driverCollection);
    }

    public MarkerRepo markerRepo(MarkerCollection markerCollection) {
        return new MarkerRepo(markerCollection);
    }
}