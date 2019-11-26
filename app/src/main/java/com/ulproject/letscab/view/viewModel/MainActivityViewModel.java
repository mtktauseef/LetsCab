package com.ulproject.letscab.view.viewModel;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.PendingResult;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.TravelMode;
import com.ulproject.letscab.Repo.DriverRepo;
import com.ulproject.letscab.Repo.MarkerRepo;
import com.ulproject.letscab.listener.FirebaseObjectValueListener;
import com.ulproject.letscab.listener.LatLngInterpolator;
import com.ulproject.letscab.model.Driver;
import com.ulproject.letscab.util.AppRxSchedulers;
import com.ulproject.letscab.util.FirebaseValueEventListenerHelper;
import com.ulproject.letscab.util.GoogleMapHelper;
import com.ulproject.letscab.util.MarkerAnimationHelper;
import com.ulproject.letscab.util.UiHelper;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;

// All of our network calls and logical implementation will be handled inside

public class MainActivityViewModel extends ViewModel implements FirebaseObjectValueListener {

    private static final String ONLINE_DRIVERS = "online_drivers";

    private static final String TAG = MainActivityViewModel.class.getSimpleName();
    private final UiHelper uiHelper;
    private final FusedLocationProviderClient locationProviderClient;
    private final DriverRepo driverRepo;
    private final GoogleMapHelper googleMapHelper;
    private final MarkerRepo markerRepo;
    private final AppRxSchedulers appRxSchedulers;

    // Getting online drivers from FireBase from root node to the reference to online_drivers.
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(ONLINE_DRIVERS);

    // You see we’re not exposing our MediatorLiveData instance publically instead we simply given a LiveData just to observe the location.
    // By doing this we’re keeping our immutability principle safe.
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    // Reverse Geo Code for Main Activity
    private MediatorLiveData<String> _reverseGeocodeResult = new MediatorLiveData<>();
    public LiveData<String> reverseGeocodeResult = _reverseGeocodeResult;
    private MediatorLiveData<Location> _currentLocation = new MediatorLiveData<>();
    public LiveData<Location> currentLocation = _currentLocation;
    private MediatorLiveData<String> _calculateDistance = new MediatorLiveData<>();
    public LiveData<String> calculateDistance = _calculateDistance;
    private MediatorLiveData<Pair<String, MarkerOptions>> _addNewMarker = new MediatorLiveData<>();
    public LiveData<Pair<String, MarkerOptions>> addNewMarker = _addNewMarker;

    // Creating Location call back and passing it to Uihelper Class
    // We are sending the location back to our MainActivity via LiveData.
    private LocationCallback locationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult.getLastLocation() == null) return;
            _currentLocation.postValue(locationResult.getLastLocation());
        }
    };
    private FirebaseValueEventListenerHelper valueEventListener;

    public MainActivityViewModel(UiHelper uiHelper, FusedLocationProviderClient locationProviderClient, DriverRepo driverRepo, MarkerRepo markerRepo, AppRxSchedulers appRxSchedulers, GoogleMapHelper googleMapHelper) {
        this.uiHelper = uiHelper;
        this.locationProviderClient = locationProviderClient;
        this.driverRepo = driverRepo;
        this.markerRepo = markerRepo;
        this.appRxSchedulers = appRxSchedulers;
        this.googleMapHelper = googleMapHelper;

        // The addChildEventListenermethod is best suitable for our use case, it will download all online Drivers from the database at application start
        // Will notify us whenever there’s a slight change in the online driver’s node.

        valueEventListener = new FirebaseValueEventListenerHelper(this);
        databaseReference.addChildEventListener(valueEventListener);
    }

    // Requesting Location Update
    @SuppressLint("MissingPermission")
    public void requestLocationUpdates() {
        locationProviderClient.requestLocationUpdates(uiHelper.getLocationRequest(), locationCallback, Looper.myLooper());
    }

    public void onCameraIdle(LatLng latLng) {
        compositeDisposable.add(Completable.fromRunnable(() -> {
            if (!driverRepo.allItems().isEmpty()) {
                Driver driver = driverRepo.getNearestDriver(latLng.latitude, latLng.longitude);
                if (driver != null)
                    calculateDistance(latLng, driver);
            } else _calculateDistance.postValue("No \n Car");
        }).subscribe(() -> {
        }, Throwable::printStackTrace));
    }

    private void calculateDistance(LatLng latLng, Driver driver) {
        compositeDisposable.add(Single.<DistanceMatrix>create(emitter -> {
            String destination[] = {String.valueOf(driver.lat) + ",".concat(String.valueOf(driver.lng))};
            String origins[] = {String.valueOf(latLng.latitude) + ",".concat(String.valueOf(latLng.longitude))};
            DistanceMatrixApi.getDistanceMatrix(googleMapHelper.geoContextDistanceApi(), origins, destination)
                    .mode(TravelMode.DRIVING)
                    .setCallback(new PendingResult.Callback<DistanceMatrix>() {
                        @Override
                        public void onResult(DistanceMatrix result) {
                            emitter.onSuccess(result);
                        }

                        @Override
                        public void onFailure(Throwable e) {
                            emitter.onError(e);
                        }
                    });
        }).subscribeOn(appRxSchedulers.io())
                .subscribe(result -> {
                    if (result == null || result.rows[0] == null || result.rows[0].elements[0] == null)
                        return;
                    _calculateDistance.postValue(String.valueOf(result.rows[0].elements[0].duration.humanReadable));
                }, Throwable::printStackTrace));
    }

    // Overriding all abstract methods and getting back the driver state
    @Override
    public void onDriverOnline(Driver driver) {
        if (driverRepo.insert(driver)) {
            MarkerOptions markerOptions = googleMapHelper.getDriverMarkerOptions(new LatLng(driver.lat, driver.lng), driver.angle);
            _addNewMarker.setValue(new Pair<>(driver.getId(), markerOptions));
            Log.e(TAG, "On driver online");

        }
    }

    public void insertDriverMarker(String key, Marker marker) {
        markerRepo.insert(key, marker);
    }

    @Override
    public void onDriverChanged(Driver driver) {
        Driver fetchedDriver = driverRepo.get(driver.getId());
        if (fetchedDriver == null) return;
        fetchedDriver.update(driver.lat, driver.lng, driver.angle);
        Marker marker = markerRepo.get(fetchedDriver.getId());
        if (marker == null) return;
        marker.setRotation(fetchedDriver.angle + 90);
        MarkerAnimationHelper.animateMarkerToGB(marker, new LatLng(fetchedDriver.lat, fetchedDriver.lng), new LatLngInterpolator.Spherical());
    }

    @Override
    public void onDriverOffline(Driver driver) {
        compositeDisposable.add(driverRepo.remove(driver.getId())
                .subscribe(b -> {
                    if (b)
                        markerRepo.remove(driver.getId());
                }, Throwable::printStackTrace));
    }
    //Reversing Geocode for the PinView coordinates

    /**
     * launch a co-routine builder so that our called thread will not be blocked. Google recommend us to Reverse Geocode in a separate thread instead of UI thread.
     * The getFromLocation() method returns an array of Addresses that are known to describe the area.
     * If the GeoCoder results are not null then we pass the result to MainActivity via LiveData to show the result in the TextView
     */

    public void makeReverseGeocodeRequest(LatLng latLng, Geocoder geocoder) {
        compositeDisposable.add(Observable.<String>create(emitter -> {
            try {
                List<Address> result = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (result != null && result.size() > 0) {
                    Address address = result.get(0);
                    emitter.onNext(address.getAddressLine(0).concat(" , ").concat(address.getLocality()));
                }
            } catch (Exception e) {
                emitter.onError(e);
            } finally {
                emitter.onComplete();
            }
        }).subscribeOn(appRxSchedulers.io())
                .subscribe(placeName -> _reverseGeocodeResult.postValue(placeName), Throwable::printStackTrace));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
        locationProviderClient.removeLocationUpdates(locationCallback);
        databaseReference.removeEventListener(valueEventListener);
        databaseReference = null;
        compositeDisposable = null;
        locationCallback = null;
    }
}