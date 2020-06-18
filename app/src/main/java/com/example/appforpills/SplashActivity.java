package com.example.appforpills;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    protected int _splashTime = 3000;
    ProgressBar progressBar;
    int counter = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                Intent i3= new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i3);
            }
        },_splashTime);
        prog();
    }

    public void prog()
    {
        progressBar =(ProgressBar) findViewById(R.id.progressBar);

        final Timer t =new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                counter++;
                progressBar.setProgress(counter);
                if(counter == 100)
                    t.cancel();

            }
        };
        t.schedule(tt, 0,_splashTime/100);
    }
}
