package com.example.appforpills;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.content.SharedPreferences;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import static java.sql.Types.NULL;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    Switch switchSound;
    Switch switchBluetooth;
    Switch switchLight;
    Switch switchMode;
    Switch switchNotifications;
    Button settingsButton;
    ImageView bluetoothOn;
    ImageView bluetoothOff;
    ImageView soundOn;
    ImageView soundOff;
    ImageView lightbulbOn;
    ImageView lightbulbOff;
    ImageView sleepModeOn;
    ImageView notificationsOff;
    RelativeLayout settingsWindow;
    RelativeLayout frame;
    RelativeLayout btStuff;
    SharedPreferences globalSettings;




    private static final int ENABLE_BT_REQUEST_CODE=1;

    BluetoothAdapter btAdapter=BluetoothAdapter.getDefaultAdapter();
    private Set<BluetoothDevice> pairedDevices;
    public static final String PREFERENCES= "globalValues";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        final MyApp application=(MyApp)getApplication();
        // Example of a call to a native method
        Button Bschedule = findViewById(R.id.schedule);
        Bschedule.setText(stringFromJNI());

//        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(,17,1, TypedValue.COMPLEX_UNIT_DIP);


        globalSettings=getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor= globalSettings.edit();
        //check if first run somehow, or only push value once
        if (!(globalSettings.contains("firstRunDone"))){
            editor.putBoolean("soundState",false);
            editor.putBoolean("isBtAvailable", true);
            editor.putBoolean("btState",false);
            editor.putBoolean("isBtConnected",false);
            editor.putBoolean("ledState",false);
            editor.putBoolean("sleepMode",false);
            editor.putBoolean("notifications",true);
            editor.putInt("volume",2);
            editor.putInt("tone",1);
            editor.putBoolean("firstRunDone",true);
            editor.apply();
        }

//        check if bt adapter is available on device
        if (btAdapter==null){
            Toast.makeText(getApplicationContext(),"This device does not support Bluetooth", Toast.LENGTH_SHORT).show();
            editor.putBoolean("isBtAvailable",false);
            editor.apply();
        }


        // sound graphics
        soundOn = findViewById(R.id.soundOn);
        soundOff = findViewById(R.id.soundOff);

        // sound switch
        switchSound = findViewById(R.id.switchSound);
        if (globalSettings.getBoolean("soundState",false)){
            soundOn.setVisibility(View.VISIBLE);
            soundOff.setVisibility(View.INVISIBLE);
            switchSound.setChecked(true);
        }else {
            soundOn.setVisibility(View.INVISIBLE);
            soundOff.setVisibility(View.VISIBLE);
            switchSound.setChecked(false);
        }

        switchSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    soundOn.setVisibility(View.VISIBLE);
                    soundOff.setVisibility(View.INVISIBLE);
//                  send packet: getLevel(); - + 0-4

                    editor.putBoolean("soundState",true).apply();

                }
                else {
                    soundOn.setVisibility(View.INVISIBLE);
                    soundOff.setVisibility(View.VISIBLE);
                    // send '0'
                    editor.putBoolean("soundState",false).apply();
                }


            }
        });


        // bluetooth graphics
        bluetoothOn = findViewById(R.id.bluetoothOn);
        bluetoothOff = findViewById(R.id.bluetoothOff);

        // bluetooth switch
        switchBluetooth = findViewById(R.id.switchBluetooth);
        switchBluetooth = findViewById(R.id.switchBluetooth);
        if (!(globalSettings.getBoolean("isBtAvailable",true))) switchBluetooth.setClickable(false);
//        if (btAdapter.isEnabled()){
////            editor.putBoolean("btState",true).apply();
//        }else {
////            editor.putBoolean("btState",false).apply();;
//        }
        if (/*globalSettings.getBoolean("btState",false)*/btAdapter.isEnabled()){
            bluetoothOn.setVisibility(View.VISIBLE);
            bluetoothOff.setVisibility(View.INVISIBLE);
            switchBluetooth.setChecked(true);
        }else {
            bluetoothOn.setVisibility(View.INVISIBLE);
            bluetoothOff.setVisibility(View.VISIBLE);
            switchBluetooth.setChecked(false);
        }

        switchBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    bluetoothOn.setVisibility(View.VISIBLE);
                    bluetoothOff.setVisibility(View.INVISIBLE);
//                    application.setBtState(true);
//                    editor.putBoolean("btState",true).apply();
                    if (!(btAdapter.isEnabled())){
                        Intent turnOnBT= new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(turnOnBT,ENABLE_BT_REQUEST_CODE);
                        Toast.makeText(getApplicationContext(), "Attempting to turn BT on", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    bluetoothOn.setVisibility(View.INVISIBLE);
                    bluetoothOff.setVisibility(View.VISIBLE);
//                    application.setBtState(false);
//                    editor.putBoolean("btState",false).apply();
                    btAdapter.disable();
                    Toast.makeText(getApplicationContext(),"This phone's Bluetooth adapter has been turned off", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // light graphics
        lightbulbOn =  findViewById(R.id.lightOn);
        lightbulbOff = findViewById(R.id.lightbulb);

        // light switch
        switchLight = findViewById(R.id.switchLight);
        if (globalSettings.getBoolean("ledState",false)){
            lightbulbOn.setVisibility(View.VISIBLE);
            switchLight.setChecked(true);
        }else {
            lightbulbOn.setVisibility(View.INVISIBLE);
            switchLight.setChecked(false);
        }

        switchLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    lightbulbOn.setVisibility(View.VISIBLE);
//                    application.setLedState(true);
                    editor.putBoolean("ledState",true).apply();
                }
                else {
                    lightbulbOn.setVisibility(View.INVISIBLE);
//                    application.setLedState(false);
                    editor.putBoolean("ledState",false).apply();
                }

            }
        });

        // open settings

        settingsButton = findViewById(R.id.settingsButton);
        settingsWindow = findViewById(R.id.settingsWindow);
        frame = findViewById(R.id.frame);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (settingsWindow.getVisibility()==View.VISIBLE) {
                    settingsWindow.setVisibility(View.INVISIBLE);
                    frame.setVisibility(View.INVISIBLE);
                    switchSound.setClickable(true);
                    switchLight.setClickable(true);
                    switchBluetooth.setClickable(true);
                }
                else {
                    settingsWindow.setVisibility(View.VISIBLE);
                    frame.setVisibility(View.VISIBLE);
                    switchSound.setClickable(false);
                    switchLight.setClickable(false);
                    switchBluetooth.setClickable(false);
                }
            }
        });

        // mode graphics
        sleepModeOn = findViewById(R.id.sleepMode);
        // mode switch
        switchMode = findViewById(R.id.switchMode);
        if (/*application.getSleepState()*/globalSettings.getBoolean("sleepMode",false)){
            sleepModeOn.setVisibility(View.VISIBLE);
            switchMode.setChecked(true);
        }else {
            sleepModeOn.setVisibility(View.INVISIBLE);
            switchMode.setChecked(false);
        }

        switchMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sleepModeOn.setVisibility(View.VISIBLE);
//                    application.setSleepState(true);
                    editor.putBoolean("sleepMode",true).apply();
                }
                else {
                    sleepModeOn.setVisibility(View.INVISIBLE);
//                    application.setSleepState(false);
                    editor.putBoolean("sleepMode", false).apply();
                }
            }
        });

        //notifications graphics
        notificationsOff = findViewById(R.id.notificationsOff);

        // notifications switch
        switchNotifications = findViewById(R.id.switchNotifications);

        if (globalSettings.getBoolean("notifications",false)/*application.getNotificationState()*/){
            notificationsOff.setVisibility(View.INVISIBLE);
            switchNotifications.setChecked(true);
        }else {
            notificationsOff.setVisibility(View.VISIBLE);
            switchNotifications.setChecked(false);
        }

        switchNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    notificationsOff.setVisibility(View.INVISIBLE);
//                    application.setNotifications(true);
                    editor.putBoolean("notifications",true).apply();
                }
                else {
                    notificationsOff.setVisibility(View.VISIBLE);
//                    application.setNotifications(false);
                    editor.putBoolean("notifications",false).apply();
                }

            }
        });


        // Change melody

        Spinner spinner = findViewById(R.id.changeMelody);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.melodies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);


//        btStuff= findViewById(R.id.btConnection);
//        btStuff.setVisibility(View.VISIBLE);
//
//        btStuff.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(btStuff.getVisibility()==View.VISIBLE){
//                    switchSound.setClickable(false);
//                    switchLight.setClickable(false);
//                    switchLight.setClickable(false);
//                }
//            }
//        }

//            );
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id ) {
    String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();

//        String[] tones;
//        tones = getResources().getStringArray(R.array.tones);
//
//        final MyApp application=(MyApp)getApplication();
//
//        char[] ch;
//        new String(ch).getBytes(tones[position]);
//        application.setTone(ch);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void settingsActivity(View v)
    {
        Intent i = new Intent(this,settings.class);
        startActivity(i);
    }

    public void aboutUsActivity(View v)
    {
        Intent j = new Intent(this,AboutUs.class);
        startActivity(j);
    }

    public void helpActivity(View v)
    {
        Intent k = new Intent(this,helpActivity.class);
        startActivity(k);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

}
