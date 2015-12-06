package pl.whiter.kote_app;

import android.app.Application;

import io.relayr.android.RelayrSdk;

/**
 * Created by whiter
 */
public class KoteApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        new RelayrSdk.Builder(this)
                .inMockMode(true)
                .build();
    }
}
