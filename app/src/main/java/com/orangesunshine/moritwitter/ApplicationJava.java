package com.orangesunshine.moritwitter;

import android.app.Application;
import android.util.Log;

import com.facebook.common.soloader.SoLoaderShim;
import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by hayatomoritani on 5/16/17.
 */

public class ApplicationJava extends Application {

    static { try { SoLoaderShim.loadLibrary("webp"); } catch(UnsatisfiedLinkError nle) {
        Log.d("tag", "AppJava static initializer: "+nle.getMessage());
    } }
    @Override
    public void onCreate() {
        super.onCreate();

        Fresco.initialize(getApplicationContext());
    }
}
