package com.example.appforpills;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class helpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
    }
    public void mainActivity(View v)
    {
        Intent j = new Intent(this,MainActivity.class);
        startActivity(j);
    }
    public void helpActivity2(View v)
    {
        Intent i = new Intent(this,helpActivity2.class);
        startActivity(i);
    }

}
