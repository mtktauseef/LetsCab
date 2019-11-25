package com.ulproject.letscab.listener;


import com.ulproject.letscab.model.Driver;

public interface FirebaseObjectValueListener {

    void onDriverOnline(Driver driver);

    void onDriverChanged(Driver driver);

    void onDriverOffline(Driver driver);

}
