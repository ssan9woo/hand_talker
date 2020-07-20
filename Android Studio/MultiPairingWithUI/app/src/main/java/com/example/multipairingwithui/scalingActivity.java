package com.example.multipairingwithui;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Arrays;

public class scalingActivity extends AppCompatActivity {

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
        isService = true;

    }

    public void onDestroy(){
        if(isService)
        {
            isService = false;
        }
        super.onDestroy();
    }


    public void btnclick(View view){
        int id=view.getId();
        switch (id){
            case R.id.btn_left_rock :
                save_flex_value(bluetoothService.LEFT,bluetoothService.ROCK,get_flex_value(bluetoothService.LEFT));
                break;
            case R.id.btn_left_paper :
                save_flex_value(bluetoothService.LEFT,bluetoothService.PAPER,get_flex_value(bluetoothService.LEFT));
                break;
            case R.id.btn_right_rock :
                save_flex_value(bluetoothService.RIGHT,bluetoothService.ROCK,get_flex_value(bluetoothService.RIGHT));
                break;
            case R.id.btn_right_paper :
                save_flex_value(bluetoothService.RIGHT,bluetoothService.PAPER,get_flex_value(bluetoothService.RIGHT));
                break;
        }
    }
    public int[] getUserdata(String name){
        int[] ret;
        int count = sharePref.getInt(bluetoothService.LEN_PREFIX + name, 0);
        ret = new int[count];
        for (int i = 0; i < count; i++){
            ret[i] = sharePref.getInt(bluetoothService.VAL_PREFIX+ name + i, i);
        }
        return ret;
    }

    public int[] get_flex_value(int id){
        int[] flex_value;
        switch (id){
            case bluetoothService.LEFT:
                flex_value = ((bluetoothService) bluetoothService.mContext).getLeftHand_Flex();
                System.out.println(Arrays.toString(flex_value));
                break;
            case bluetoothService.RIGHT:
                flex_value = ((bluetoothService) bluetoothService.mContext).getRightHand_Flex();
                System.out.println(Arrays.toString(flex_value));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + id);
        }

        return flex_value;
    }
    public void save_flex_value(int id, int rock_or_paper,int[] flex_values){
        String name = bluetoothService.str_hand[id]+bluetoothService.str_rock_or_paper[rock_or_paper];
        editor.putInt(bluetoothService.LEN_PREFIX + name, flex_values.length);
        int count = 0;
        for (int i: flex_values){
            editor.putInt(bluetoothService.VAL_PREFIX + name + count++, i);
        }
        editor.apply();
    }
}
