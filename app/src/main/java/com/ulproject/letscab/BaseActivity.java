package com.ulproject.letscab;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ulproject.letscab.application.MyCustomApplication;
import com.ulproject.letscab.di.component.ActivityComponent;
import com.ulproject.letscab.di.component.DaggerActivityComponent;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    protected ActivityComponent activityComponent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent = DaggerActivityComponent
                .builder()
                .appComponent(getApp().appComponent())
                .build();
    }

    private MyCustomApplication getApp() {
        return (MyCustomApplication) getApplicationContext();
    }
}
