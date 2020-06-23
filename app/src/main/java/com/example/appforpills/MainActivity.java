package com.example.appforpills;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


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
    Button btHideButton;
    Button btShowButton;
    Button settingsButton;
    Button discoverButton;
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
    ArrayAdapter pairedDeviceList;
    ArrayAdapter discoveredDeviceList;
    ListView pairedDeviceListView;
    ListView discoveredDeviceListView;
    Set<BluetoothDevice> pairedDevices;
    Set<BluetoothDevice> discoveredDevices;
    TextView btName;
    TextView btDiscoveredName;

    private static final int ENABLE_BT_REQUEST_CODE=1;
    private static final String PREFERENCES= "globalValues";
    public final BluetoothAdapter btAdapter=BluetoothAdapter.getDefaultAdapter();

    public void makeList(){
        pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size()>0) {
            pairedDeviceList.clear();
            for (BluetoothDevice device : pairedDevices) pairedDeviceList.add(device.getName());
        }
        else pairedDeviceList.add("No devices paired or Bluetooth turned off");
        pairedDeviceList.notifyDataSetChanged();
    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
//                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                discoveredDevices.add(device);

                discoveredDeviceList.add(device.getName());
                discoveredDeviceList.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode,int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ENABLE_BT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth has been enabled", Toast.LENGTH_SHORT).show();
                switchBluetooth.setChecked(true);
                makeList();
            }
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "Failed to enable Bluetooth", Toast.LENGTH_SHORT).show();
            switchBluetooth.setChecked(false);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Example of a call to a native method
        Button Bschedule = findViewById(R.id.schedule);
        Bschedule.setText(stringFromJNI());

        btName=findViewById(R.id.btConnectionName);
        btDiscoveredName=findViewById(R.id.btConnectionNameDiscovered);

        pairedDeviceList = new ArrayAdapter(getApplicationContext(),android.R.layout.simple_list_item_1);
        discoveredDeviceList = new ArrayAdapter(getApplicationContext(),android.R.layout.simple_list_item_1);

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
        pairedDeviceListView=findViewById(R.id.pairedDevices);
        pairedDeviceListView.setAdapter(pairedDeviceList);

        discoveredDeviceListView=findViewById(R.id.discoveredDevices);
        discoverButton=findViewById(R.id.discoverButton);
        discoveredDeviceListView.setAdapter(discoveredDeviceList);

        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btAdapter.isDiscovering()) btAdapter.cancelDiscovery();
                btAdapter.startDiscovery();
                    Toast.makeText(getApplicationContext(), "Discovering other bluetooth devices...", Toast.LENGTH_SHORT).show();
                    pairedDeviceListView.setVisibility(View.INVISIBLE);
                    discoveredDeviceListView.setVisibility(View.VISIBLE);
                    btName.setVisibility(View.INVISIBLE);
                    btDiscoveredName.setVisibility(View.VISIBLE);
//                }else{
//                    Toast.makeText(getApplicationContext(), "Something went wrong! Discovery has failed to start.", Toast.LENGTH_SHORT).show();
//                }

            }
        });
//        check if bt adapter is available on device
        if (btAdapter==null){
            Toast.makeText(getApplicationContext(),"This device does not support Bluetooth", Toast.LENGTH_SHORT).show();
            editor.putBoolean("isBtAvailable",false);
            editor.apply();
        }
        pairedDevices = btAdapter.getBondedDevices();

        makeList();
        //        Get paired devices and adapt the for ListView

//        pairedDeviceList = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,pairedDevices);
//        makeList();

        pairedDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });


        //btmenu
        btHideButton=findViewById(R.id.buttonCloseBTTest);
        btShowButton=findViewById(R.id.btShowButtonTest);
        if (!(btAdapter.isEnabled())) btShowButton.setVisibility(View.INVISIBLE);
        btStuff=findViewById(R.id.btConnection);
        btHideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btStuff.getVisibility()==View.VISIBLE){
                    btStuff.setVisibility(View.INVISIBLE);
                    btHideButton.setVisibility(View.INVISIBLE);
                    if (btAdapter.isEnabled()) btShowButton.setVisibility(View.VISIBLE);
                }
            }
        });

        btShowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if (btStuff.getVisibility()==View.INVISIBLE){
                    btShowButton.setVisibility(View.INVISIBLE);
                    btHideButton.setVisibility(View.VISIBLE);
                    btStuff.setVisibility(View.VISIBLE);
                }
                if(btAdapter.isEnabled()) makeList();
            }
        });

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
        if (btAdapter.isEnabled()){
            bluetoothOn.setVisibility(View.VISIBLE);
            bluetoothOff.setVisibility(View.INVISIBLE);
            switchBluetooth.setChecked(true);
            if (btStuff.getVisibility()==View.INVISIBLE){
                btShowButton.setVisibility(View.INVISIBLE);
                btHideButton.setVisibility(View.VISIBLE);
                btStuff.setVisibility(View.VISIBLE);
            }
        }else {
            bluetoothOn.setVisibility(View.INVISIBLE);
            bluetoothOff.setVisibility(View.VISIBLE);
            switchBluetooth.setChecked(false);
            if (btStuff.getVisibility()==View.VISIBLE){
                btStuff.setVisibility(View.INVISIBLE);
                btHideButton.setVisibility(View.INVISIBLE);
                btShowButton.setVisibility(View.VISIBLE);
            }
        }

        switchBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    bluetoothOn.setVisibility(View.VISIBLE);
                    bluetoothOff.setVisibility(View.INVISIBLE);
                    if (!(btAdapter.isEnabled())){
                        Intent turnOnBT= new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(turnOnBT,ENABLE_BT_REQUEST_CODE);
                        Toast.makeText(getApplicationContext(), "Attempting to turn BT on", Toast.LENGTH_SHORT).show();
                    }
                    final Handler delay2sec = new Handler();
                    delay2sec.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (btStuff.getVisibility()==View.INVISIBLE){
                                btShowButton.setVisibility(View.INVISIBLE);
                                btHideButton.setVisibility(View.VISIBLE);
                                btStuff.setVisibility(View.VISIBLE);
                            }
                        }
                    },2000);
                }
                else {
                    bluetoothOn.setVisibility(View.INVISIBLE);
                    bluetoothOff.setVisibility(View.VISIBLE);
                    btAdapter.disable();
                    btStuff.setVisibility(View.INVISIBLE);
                    btHideButton.setVisibility(View.INVISIBLE);
                    btShowButton.setVisibility(View.INVISIBLE);
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
        pairedDeviceListView.setAdapter(pairedDeviceList);
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

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (btAdapter.isDiscovering()) btAdapter.cancelDiscovery();
        this.unregisterReceiver(mReceiver);
    }

}
