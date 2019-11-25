package com.ulproject.letscab.view.viewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.ulproject.letscab.Repo.DriverRepo;
import com.ulproject.letscab.Repo.MarkerRepo;
import com.ulproject.letscab.util.AppRxSchedulers;
import com.ulproject.letscab.util.GoogleMapHelper;
import com.ulproject.letscab.util.UiHelper;

public class MainActivityViewModelFactory implements ViewModelProvider.Factory {

    private final UiHelper uiHelper;
    private final FusedLocationProviderClient locationProviderClient;
    private final AppRxSchedulers appRxSchedulers;
    private final DriverRepo driverRepo;
    private final MarkerRepo markerRepo;
    private final GoogleMapHelper googleMapHelper;

    public MainActivityViewModelFactory(UiHelper uiHelper, FusedLocationProviderClient locationProviderClient, AppRxSchedulers appRxSchedulers, DriverRepo driverRepo, MarkerRepo markerRepo, GoogleMapHelper googleMapHelper) {
        this.uiHelper = uiHelper;
        this.locationProviderClient = locationProviderClient;
        this.appRxSchedulers = appRxSchedulers;
        this.driverRepo = driverRepo;
        this.markerRepo = markerRepo;
        this.googleMapHelper = googleMapHelper;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MainActivityViewModel(uiHelper, locationProviderClient, driverRepo, markerRepo, appRxSchedulers, googleMapHelper);
    }
}
