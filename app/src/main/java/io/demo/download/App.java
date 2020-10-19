package io.demo.download;

import android.app.Application;


/**
 *
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }
}
