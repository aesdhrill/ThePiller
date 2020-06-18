package com.example.appforpills;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class helpActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help2);
    }
    public void mainActivity(View v)
    {
        Intent j = new Intent(this,MainActivity.class);
        startActivity(j);
    }
    public void helpActivity(View v)
    {
        Intent i = new Intent(this,helpActivity.class);
        startActivity(i);
    }

}
