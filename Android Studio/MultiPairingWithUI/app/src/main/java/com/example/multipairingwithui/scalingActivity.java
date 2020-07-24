package com.example.multipairingwithui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.MenuItem;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.multipairingwithui.bluetoothService.MyBinder;
import com.google.android.material.navigation.NavigationView;

import java.util.Arrays;

public class scalingActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //Variables
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    Context mContext;
    private boolean isService;
    private Messenger mServiceMessenger = null;
    bluetoothService mService; // 서비스 객체
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scaling);
        String SHARE_NAME = "SHARE_PREF";
        isService = true;
        mContext=this;
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        /*--------------------------Tool Bar----------------------------*/
        setSupportActionBar(toolbar);

        /*--------------------------Navigation Drawer Menu----------------------------*/

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem( R.id.nav_scaling );
        bindService(new Intent(scalingActivity.this,bluetoothService.class), conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onBackPressed() {

        if( drawerLayout.isDrawerOpen( GravityCompat.START ) ){

            drawerLayout.closeDrawer( GravityCompat.START );
        }
        else{
            super.onBackPressed();
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch ( menuItem.getItemId() ){
            case R.id.nav_home:
                Intent intent_home = new Intent( scalingActivity.this, MainActivity.class);
                startActivity( intent_home );
                break;
            case R.id.nav_add_del:
                Intent intent_add = new Intent( scalingActivity.this, AddDelActivity.class);
                startActivity(intent_add);
                break;
            case R.id.nav_scaling:
                break;
        }
        drawerLayout.closeDrawer( GravityCompat.START );
        finish();
        return true;
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceMessenger = new Messenger(service);
            try{
                Message msg = Message.obtain(null,1);
                msg.replyTo = mMessenger;
                mServiceMessenger.send(msg);
            } catch (RemoteException ignored) {
            }
            Toast.makeText(getApplicationContext(), "Service Connected", Toast.LENGTH_LONG).show();
            isService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isService = false;
        }
    };
    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            return false;
        }
    }));
    public void onDestroy(){
        if(isService)
        {
            isService = false;
        }
        unbindService(conn);
        super.onDestroy();
    }

    public void btnclick(View view){
        int id=view.getId();
        switch (id){
            case R.id.btn_left_rock :
                PreferenceManager.save_flex_value(bluetoothService.LEFT,bluetoothService.ROCK,bluetoothService.left_rowflex,mContext);
                break;
            case R.id.btn_left_paper :
                PreferenceManager.save_flex_value(bluetoothService.LEFT,bluetoothService.PAPER,bluetoothService.left_rowflex,mContext);
                break;
            case R.id.btn_right_rock :
                PreferenceManager.save_flex_value(bluetoothService.RIGHT,bluetoothService.ROCK,bluetoothService.right_rowflex,mContext);
                break;
            case R.id.btn_right_paper :
                PreferenceManager.save_flex_value(bluetoothService.RIGHT,bluetoothService.PAPER,bluetoothService.right_rowflex,mContext);
                break;
        }
    }
}
