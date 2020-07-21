package com.example.multipairingwithui;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class bluetoothReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        assert action != null;
        if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            assert device != null;
            if(device.getAddress().equals("00:18:E4:34:D4:8B"))
            {
                ((bluetoothService) bluetoothService.mContext).disconnectRight();
            }

            if(device.getAddress().equals("00:18:91:D8:36:42"))
            {
                ((bluetoothService) bluetoothService.mContext).disconnectLeft();
            }
        }
    }
}