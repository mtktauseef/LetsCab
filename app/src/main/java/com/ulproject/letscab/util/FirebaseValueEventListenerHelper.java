package com.ulproject.letscab.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.ulproject.letscab.listener.FirebaseObjectValueListener;
import com.ulproject.letscab.model.Driver;

public class FirebaseValueEventListenerHelper implements ChildEventListener {

    private final FirebaseObjectValueListener firebaseObjectValueListener;

    /**
     * These will be triggered whenever there’s a new Driver added inside the online_drivers node.
     * Notice how we parse the Driver model, by calling the getValue method, the snapshot is parsed to whatever data model you want.
     * After parsing the data we simply call the onDriverOnline method, which implemented inside the MainActivityViewModel class.
     * This method will be triggered whenever the Driver node update in the online_drivers node.
     * The onChildRemoved method will be called when there’s a Driver goes offline inside the online_drivers node.
     */

    //  abstract methods
    public FirebaseValueEventListenerHelper(FirebaseObjectValueListener firebaseObjectValueListener) {
        this.firebaseObjectValueListener = firebaseObjectValueListener;
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        Driver driver = dataSnapshot.getValue(Driver.class);
        firebaseObjectValueListener.onDriverOnline(driver);
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        Driver driver = dataSnapshot.getValue(Driver.class);
        firebaseObjectValueListener.onDriverChanged(driver);
    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
        Driver driver = dataSnapshot.getValue(Driver.class);
        firebaseObjectValueListener.onDriverOffline(driver);
    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

}
