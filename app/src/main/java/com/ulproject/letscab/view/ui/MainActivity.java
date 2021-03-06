package com.ulproject.letscab.view.ui;

import android.Manifest;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.ulproject.letscab.BaseActivity;
import com.ulproject.letscab.R;
import com.ulproject.letscab.Repo.DriverRepo;
import com.ulproject.letscab.Repo.MarkerRepo;
import com.ulproject.letscab.listener.LatLngInterpolator;
import com.ulproject.letscab.util.AppRxSchedulers;
import com.ulproject.letscab.util.GoogleMapHelper;
import com.ulproject.letscab.util.MarkerAnimationHelper;
import com.ulproject.letscab.util.UiHelper;
import com.ulproject.letscab.view.viewModel.MainActivityViewModel;
import com.ulproject.letscab.view.viewModel.MainActivityViewModelFactory;

import javax.inject.Inject;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends BaseActivity implements GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveStartedListener {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 5655;

    @Inject
    UiHelper uiHelper;
    @Inject
    GoogleMapHelper googleMapHelper;
    @Inject
    AppRxSchedulers appRxSchedulers;
    @Inject
    DriverRepo driverRepo;
    @Inject
    MarkerRepo markerRepo;

    private TextView currentPlaceTextView;
    private TextView pinTimeTextView;
    private ProgressBar pinProgressLoader;

    private GoogleMap googleMap;
    private Marker currentLocationMarker;
    private MainActivityViewModel viewModel;
    private Geocoder geocoder;

    private boolean firstTimeFlag = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contenemain_new);
        activityComponent.inject(this);

        Intent intent = getIntent();

        MainActivityViewModelFactory factory = new MainActivityViewModelFactory(uiHelper,
                LocationServices.getFusedLocationProviderClient(this),
                appRxSchedulers,
                driverRepo,
                markerRepo,
                googleMapHelper
        );

        viewModel = ViewModelProviders.of(this, factory).get(MainActivityViewModel.class);
        geocoder = new Geocoder(this);
        initViews();

        if (!uiHelper.isPlayServicesAvailable()) {
            uiHelper.toast("Play services is not installed!");
            finish();
        } else requestLocationUpdates();

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert supportMapFragment != null;
        supportMapFragment.getMapAsync(googleMap -> {
            this.googleMap = googleMap;
            setGoogleMapSettings();
        });

        // Getting Place Name of current location from Main Activity View Model
        viewModel.reverseGeocodeResult
                .observe(this, placeName -> currentPlaceTextView.setText(placeName));

        // observe the user’s current location from MainActivityViewModel.
        viewModel.currentLocation
                .observe(this, location -> {
                    if (firstTimeFlag) {
                        firstTimeFlag = false;
                        animateCamera(location);
                    }
                    showOrAnimateMarker(location);
                });

        viewModel.addNewMarker
                .observe(this, markerPair -> {
                    if (googleMap != null && markerPair != null) {
                        Marker marker = googleMap.addMarker(markerPair.second);
                        viewModel.insertDriverMarker(markerPair.first, marker);
                    }
                });

        viewModel.calculateDistance
                .observe(this, distance -> {
                    pinTimeTextView.setText(distance);
                    pinTimeTextView.setVisibility(VISIBLE);
                    pinProgressLoader.setVisibility(GONE);
                });
    }

    private void initViews() {
        currentPlaceTextView = findViewById(R.id.currentPlaceTextView);
        pinProgressLoader = findViewById(R.id.pinProgressLoader);
        pinTimeTextView = findViewById(R.id.pinTimeTextView);
        findViewById(R.id.currentLocationImageButton).setOnClickListener(__ -> {
            Location location = viewModel.currentLocation.getValue();
            if (location == null || googleMap == null) return;
            animateCamera(location);
        });
    }

    private void requestLocationUpdates() {
        if (!uiHelper.isHaveLocationPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
        if (uiHelper.isLocationProviderEnabled())
            uiHelper.showPositiveDialogWithListener(this, getResources().getString(R.string.need_location), getResources().getString(R.string.location_content), () -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)), "Turn On", false);
        viewModel.requestLocationUpdates();
    }

    private void animateCamera(Location location) {
        CameraUpdate cameraUpdate = googleMapHelper.buildCameraUpdate(location);
        googleMap.animateCamera(cameraUpdate, 10, null);
    }

    private void showOrAnimateMarker(Location location) {
        if (currentLocationMarker == null)
            currentLocationMarker = googleMap.addMarker(googleMapHelper.getUserMarker(location));
        else
            MarkerAnimationHelper.animateMarkerToGB(currentLocationMarker, location, new LatLngInterpolator.Spherical());
    }

    private void setGoogleMapSettings() {
        googleMapHelper.defaultMapSettings(googleMap);
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
        googleMap.setOnCameraIdleListener(this);
        googleMap.setOnCameraMoveStartedListener(this);
    }

    // OnCameraIdleListener on MainActivity. The OnCameraIdleListener has its callback function which defines what actions needs to done when the camera stopped its movement.
    // In this callback, we need to get the coordinates from PinView and execute our Reverse Geocode request.
    @Override
    public void onCameraIdle() {
        if (googleMap == null) return;
        LatLng latLng = googleMap.getCameraPosition().target;
        viewModel.makeReverseGeocodeRequest(latLng, geocoder);
        viewModel.onCameraIdle(latLng);
    }

    @Override
    public void onCameraMoveStarted(int i) {
        pinTimeTextView.setVisibility(GONE);
        pinProgressLoader.setVisibility(VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PERMISSION_DENIED) {
                RelativeLayout mainActivityRootView = findViewById(R.id.mainActivityRootView);
                uiHelper.showSnackBar(mainActivityRootView, getResources().getString(R.string.permission_needed));
            }
            if (grantResults[0] == PERMISSION_GRANTED)
                requestLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel = null;
        uiHelper = null;
        googleMapHelper = null;
        currentLocationMarker = null;
        appRxSchedulers = null;
        googleMap = null;
        markerRepo = null;
        driverRepo = null;
        geocoder = null;
    }
}