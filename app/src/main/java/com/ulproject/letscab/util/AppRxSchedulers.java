package com.ulproject.letscab.util;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AppRxSchedulers {

    public AppRxSchedulers() {

    }

    public Scheduler io() {
        return Schedulers.io();
    }

    public Scheduler computation() {
        return Schedulers.computation();
    }

    public Scheduler mainThread() {
        return AndroidSchedulers.mainThread();
    }

    public Scheduler newThread() {
        return Schedulers.newThread();
    }
}