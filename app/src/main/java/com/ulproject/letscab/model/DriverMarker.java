package com.ulproject.letscab.model;

import com.google.android.gms.maps.model.MarkerOptions;

public class DriverMarker {

    public String id;
    public MarkerOptions markerOptions;

    public DriverMarker(String id, MarkerOptions markerOptions) {
        this.id = id;
        this.markerOptions = markerOptions;
    }
}
