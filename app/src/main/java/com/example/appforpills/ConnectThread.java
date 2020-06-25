package com.example.appforpills;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;


public class ConnectThread extends Thread {
    private static final UUID uuid=UUID.fromString("f3a298ff-89b0-4c40-be43-5902a9fc8202");
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final BluetoothAdapter mmAdapter=BluetoothAdapter.getDefaultAdapter();

    public ConnectThread(BluetoothDevice device){
        BluetoothSocket tmp=null;
        mmDevice=device;
        try{
            tmp=device.createRfcommSocketToServiceRecord(uuid);
        }catch (IOException e){
            Log.e("socketCreateError","Socket's create() failed",e);
        }
        mmSocket=tmp;
    }

    public void run(){
        mmAdapter.cancelDiscovery();

        try{
            mmSocket.connect();
        } catch (IOException connectException){
            try{
                mmSocket.close();
            }catch(IOException closeException){
                Log.e("socketCloseError","Could not close the client socket",closeException);
            }
            return;
        }
        //if succeeded
//        manageConnectedSocket(mmSocket);
    }

    public void cancel(){
        try{
            mmSocket.close();
        }catch(IOException e){
            Log.e("socketCloseError","Could not close the client socket",e);
        }
    }
}
