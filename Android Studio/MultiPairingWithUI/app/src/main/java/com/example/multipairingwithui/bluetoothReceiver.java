package com.example.multipairingwithui;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.io.IOException;

public class bluetoothReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if(device.getAddress().equals("00:18:E4:34:D4:8E"))
            {
                ((bluetoothService) bluetoothService.mContext).disconnectLeft();
            }

            if(device.getAddress().equals("00:18:91:D8:36:42"))
            {
                ((bluetoothService) bluetoothService.mContext).disconnectRight();
            }
        }
    }
}