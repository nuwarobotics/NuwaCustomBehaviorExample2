package com.nuwarobotics.example.custombehavior;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;

public class Main extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Start CustomBehavior Service
        Intent intent = new Intent();
        intent.setClassName(this.getPackageName(), this.getPackageName() + ".service.CustomService");
        //Start service as foreground service.
        //Android 8.0 not allow background service, so here use startForegroundService.
        //Reference : https://developer.android.com/about/versions/oreo/android-8.0-changes#back-all
        startForegroundService(intent);

        //Force UI back to Robot Face
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
        //Closed app
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

}
