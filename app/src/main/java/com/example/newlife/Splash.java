package com.example.newlife;

import android.content.Intent;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!SharedPrefManager.getInstance(Splash.this).isLoggedIn()) {
                    startActivity(new Intent(Splash.this, ScanCode.class));
                    finish();
                } else {
                    startActivity(new Intent(Splash.this, MainActivity.class));
                    finish();
                }
            }
        }, 3000);


    }
}
