package com.nuwarobotics.example.custombehavior;

import android.app.Application;
import android.content.Context;

public class NuwaApplication extends Application {

    private static String TAG = "NuwaApplication";
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }

}
