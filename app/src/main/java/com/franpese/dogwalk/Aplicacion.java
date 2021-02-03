package com.franpese.dogwalk;

import android.app.Application;

import timber.log.Timber;

public class Aplicacion extends Application {

    public Aplicacion() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(BuildConfig.DEBUG){ Timber.plant(new Timber.DebugTree()); }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

}
