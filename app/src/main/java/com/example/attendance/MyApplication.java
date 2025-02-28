package com.example.attendance;

import android.app.Application;

import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Map config = new HashMap();
        config.put("cloud_name", "ds12ivy94");
        config.put("api_key", "356955384385763");
        config.put("api_secret", "E0-zZv_zjqEeuq4nJY1pRmxbFlE"); // Needed for some operations

        MediaManager.init(this, config);
}
}