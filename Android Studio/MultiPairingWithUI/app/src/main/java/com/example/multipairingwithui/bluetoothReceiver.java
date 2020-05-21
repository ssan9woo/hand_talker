package com.example.multipairingwithui;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class bluetoothReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();


        if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if(device.getName().equals("sign"))
            {
                ((bluetoothService) bluetoothService.mContext).disconnectRight();
            }

            if(device.getName().equals("HC-06"))
            {
                ((bluetoothService) bluetoothService.mContext).disconnectLeft();
            }
        }
    }
}