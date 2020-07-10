package com.example.multipairingwithui;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.multipairingwithui.bluetoothService.MyBinder;

import java.util.Arrays;

public class scalingActivity extends AppCompatActivity {
    static final int LEFT =1;
    static final int RIGHT =2;
    static SharedPreferences sharePref = null;
    static SharedPreferences.Editor editor = null;

    private bluetoothService ms;
    private boolean isService;
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scaling);
        String SHARE_NAME = "SHARE_PREF";
        sharePref = getSharedPreferences(SHARE_NAME,MODE_PRIVATE);
        editor =sharePref.edit();
        bindService(new Intent(this, bluetoothService.class), conn, Context.BIND_AUTO_CREATE);
        isService = true;
    }
    public void onDestroy(){
        if(isService)
        {
            unbindService(conn);
            isService = false;
        }
        stopService(new Intent(this,bluetoothService.class));
        super.onDestroy();
    }
    public void btnclick(View view){
        int id=view.getId();
        switch (id){
            case R.id.btn_left_rock :
            case R.id.btn_left_paper :
                scale_flex_value(LEFT);
                break;
            case R.id.btn_right_rock :
            case R.id.btn_right_paper :
                scale_flex_value(RIGHT);
                break;
        }
    }
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyBinder mb = (MyBinder) iBinder;
            ms = mb.getService();
            Toast.makeText(getApplicationContext(), "Service Connected", Toast.LENGTH_LONG).show();
            isService = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isService=false;
        }
    };

    public void scale_flex_value(int id){
        int[] flexvalue;
        switch (id){
            case LEFT:
                flexvalue = ms.getLeftHand_Flex();
                System.out.println(Arrays.toString(flexvalue));
                break;
            case RIGHT:
                flexvalue =ms.getRightHand_Flex();
                System.out.println(Arrays.toString(flexvalue));
                break;
        }
    }
}
