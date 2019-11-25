package com.ulproject.letscab.Repo;

import com.ulproject.letscab.collections.DriverCollection;
import com.ulproject.letscab.model.Driver;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;


public class DriverRepo {

    private final DriverCollection driverCollection;

    @Inject
    public DriverRepo(DriverCollection driverCollection) {
        this.driverCollection = driverCollection;
    }

    public boolean insert(Driver driver) {
        return driverCollection.insertDriver(driver);
    }

    public Observable<Boolean> remove(String s) {
        return driverCollection.removeDriver(s);
    }

    public Driver get(String s) {
        return driverCollection.getDriverWithId(s);
    }

    public Driver getNearestDriver(double lat, double lng) {
        return driverCollection.getNearestDriver(lat, lng);
    }

    public List<Driver> allItems() {
        return driverCollection.allDriver();
    }
}