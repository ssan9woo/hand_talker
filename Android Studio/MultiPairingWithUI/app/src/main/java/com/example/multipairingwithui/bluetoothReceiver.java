package com.example.multipairingwithui;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class bluetoothReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        assert action != null;
        if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            assert device != null;
            if(device.getName().equals("Right"))
            {
                ((bluetoothService) bluetoothService.mContext).disconnectRight();
            }

            if(device.getName().equals("Left"))
            {
                ((bluetoothService) bluetoothService.mContext).disconnectLeft();
            }
        }
    }
}