package com.example.multipairingwithui;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final int LEFT =0;
    public static final int RIGHT =1;
    public static final int BOTH =100;

    @SuppressLint("StaticFieldLeak")
    public static Context mainContext;
    private Messenger mServiceMessenger = null;
    boolean isService = false;

    ImageView leftRock;
    ImageView leftPaper;
    ImageView rightRock;
    ImageView rightPaper;
    ImageView signImage;

    //--------Right Hand---------
    Button reconnectRight;
    TextView bluetoothStateRight;

    //-------Left Hand----------
    Button reconnectLeft;
    TextView bluetoothStateLeft;

    //animation
    Animation fadeOutAnimation;
    Animation fadeInAnimation;
    Animation clearAnimation;
    TextView signMessage;

    //Variables
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    BluetoothAdapter BA;
    @SuppressLint({"SetTextI18n", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BA = BluetoothAdapter.getDefaultAdapter();
        if(!BA.isEnabled()){
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(i,5000);
        }

        leftRock = findViewById(R.id.leftRock);
        leftPaper = findViewById(R.id.leftPaper);
        rightRock = findViewById(R.id.rightRock);
        rightPaper = findViewById(R.id.rightPaper);
        rightPaper.setVisibility(View.INVISIBLE);
        leftPaper.setVisibility(View.INVISIBLE);


        signImage = findViewById(R.id.signImage);
        signImage.setVisibility(View.INVISIBLE);
        signMessage = findViewById(R.id.signMessage);
        signMessage.setVisibility(View.INVISIBLE);

        //----------------------Find VIEW---------------------------------//
        reconnectRight = findViewById(R.id.reconnectRight);
        reconnectLeft = findViewById(R.id.reconnectLeft);
        bluetoothStateRight = findViewById(R.id.bluetoothStateRight);
        bluetoothStateLeft = findViewById(R.id.bluetoothStateLeft);

        mainContext = this;
        reconnectRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((bluetoothService)bluetoothService.mContext).reconnectRight();
            }
        });

        reconnectLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((bluetoothService)bluetoothService.mContext).reconnectLeft();
            }
        });

        fadeOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadeout);
        fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
        clearAnimation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.clear);

        /*-----------------------------------Hooks-------------------------------------*/
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
        navigationView.setCheckedItem( R.id.nav_home );
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
                break;
            case R.id.nav_add_del:
                Intent intent_add = new Intent( MainActivity.this, AddDelActivity.class);
                startActivity(intent_add);
                break;
            case R.id.nav_scaling:
                Intent intent_scaling = new Intent( MainActivity.this, scalingActivity.class);
                startActivity(intent_scaling);
                break;
        }
        drawerLayout.closeDrawer( GravityCompat.START );
        finish();
        return true;
    }

    public void onStart() {
        super.onStart();
        startService(new Intent(this, bluetoothService.class));
        bindService(new Intent(this,bluetoothService.class), conn, Context.BIND_AUTO_CREATE);
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

    //초기상태 [왼손 상태 -> 왼손 연결중, Handler로 5초뒤에 연결됨 아닐시 연결 안된것으로 판단, 연결끊킴으로 넘어가기]
    //방법1. 5초뒤에 연결됨 아닐시에 service에서 sendMessage하는 함수 호출
    //sendMessage -> mainActivity로 disconnect 보내기
    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == RIGHT) {
                switch(msg.arg1)
                {
                    case bluetoothService.DISCONNECT:
                        bluetoothStateRight.setVisibility(View.VISIBLE);
                        bluetoothStateRight.setText("오른손 연결끊김");
                        rightPaper.setVisibility(View.INVISIBLE);
                        rightRock.setVisibility(View.VISIBLE);
                        reconnectRight.setVisibility(View.VISIBLE);
                        break;
                    case bluetoothService.CONNECTED:
                        bluetoothStateRight.setText("오른손 연결됨");
                        reconnectRight.setVisibility(View.INVISIBLE);
                        rightRock.setVisibility(View.INVISIBLE);
                        rightPaper.setVisibility(View.VISIBLE);
                        break;
                    case bluetoothService.CONNECTING:
                        bluetoothStateRight.setText("오른손 연결중");
                        break;
                }
            }
            else if(msg.what == LEFT)
            {
                switch(msg.arg1)
                {
                    case bluetoothService.DISCONNECT:
                        bluetoothStateLeft.setVisibility(View.VISIBLE);
                        bluetoothStateLeft.setText("왼손 연결끊김");
                        leftPaper.setVisibility(View.INVISIBLE);
                        leftRock.setVisibility(View.VISIBLE);
                        reconnectLeft.setVisibility(View.VISIBLE);
                        break;
                    case bluetoothService.CONNECTED:
                        bluetoothStateLeft.setText("왼손 연결됨");
                        reconnectLeft.setVisibility(View.INVISIBLE);
                        leftRock.setVisibility(View.INVISIBLE);
                        leftPaper.setVisibility(View.VISIBLE);
                        break;
                    case bluetoothService.CONNECTING:
                        bluetoothStateLeft.setText("왼손 연결중");
                        break;
                }
            }
            else if(msg.what == BOTH)
            {
                bluetoothStateLeft.setVisibility(View.INVISIBLE);
                bluetoothStateRight.setVisibility(View.INVISIBLE);
                reconnectLeft.setVisibility(View.INVISIBLE);
                reconnectRight.setVisibility(View.INVISIBLE);

                //animation
                rightPaper.startAnimation(fadeOutAnimation);
                leftPaper.startAnimation(fadeOutAnimation);
                signImage.startAnimation(fadeInAnimation);

                Handler mHandler = new Handler();
                mHandler.postDelayed(new Runnable()  {
                    public void run() {
                        rightPaper.startAnimation(clearAnimation);
                        leftPaper.startAnimation(clearAnimation);
                        signImage.startAnimation(fadeOutAnimation);
                        signMessage.startAnimation(fadeOutAnimation);
                    }
                }, 500); // 0.5초후
            }
            return false;
        }
    }));

    private void sendMessageToService(Message msg){
        if(isService){
            if(mServiceMessenger != null){
                try{
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 5000) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }
}