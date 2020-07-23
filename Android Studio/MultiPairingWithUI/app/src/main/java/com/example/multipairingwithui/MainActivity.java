package com.example.multipairingwithui;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public static final int LEFT =0;
    public static final int RIGHT =1;
    public static final int BOTH =100;

    @SuppressLint("StaticFieldLeak")
    public static Context mainContext;
    private Messenger mServiceMessenger = null;
    boolean isService = false;
    //Timer
    private TimerTask Task;
    private Timer Timer;

    ImageView leftRock;
    ImageView leftPaper;
    ImageView rightRock;
    ImageView rightPaper;
    ImageView signImage,batHighRight,batHighLeft,batMiddleRight,batMiddleLeft,batLowRight,batLowLeft;

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
        //battery
        batHighLeft = findViewById(R.id.batHighLeft);
        batHighRight = findViewById(R.id.batHighRight);
        batMiddleLeft = findViewById(R.id.batMiddleLeft);
        batMiddleRight = findViewById(R.id.batMiddleRight);
        batLowLeft = findViewById(R.id.batLowLeft2);
        batLowRight = findViewById(R.id.batLowRight);

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
                Task = new TimerTask() {
                    @Override
                    public void run() {
                        String voltRight = ((bluetoothService) bluetoothService.mContext).voltRight;
                        String voltLeft = ((bluetoothService) bluetoothService.mContext).voltLeft;

                        if (voltRight.equals("") && voltLeft.equals("")) {

                            double l_volt = Double.parseDouble(voltLeft) * 20.0;
                            double r_volt = Double.parseDouble(voltRight) * 20.0;

                            if (l_volt >= 0 && l_volt <= 30) {
                                batLowLeft.setVisibility(View.VISIBLE);
                                batMiddleLeft.setVisibility(View.INVISIBLE);
                                batHighLeft.setVisibility(View.INVISIBLE);
                            } else if (l_volt > 30 && l_volt <= 60) {
                                batLowLeft.setVisibility(View.INVISIBLE);
                                batMiddleLeft.setVisibility(View.VISIBLE);
                                batHighLeft.setVisibility(View.INVISIBLE);
                            } else if (l_volt > 60 && l_volt <= 100) {
                                batLowLeft.setVisibility(View.INVISIBLE);
                                batMiddleLeft.setVisibility(View.INVISIBLE);
                                batHighLeft.setVisibility(View.VISIBLE);
                            }

                            //right
                            if (r_volt >= 0 && r_volt <= 30) {
                                batLowRight.setVisibility(View.VISIBLE);
                                batMiddleRight.setVisibility(View.INVISIBLE);
                                batHighRight.setVisibility(View.INVISIBLE);
                            } else if (r_volt > 30 && r_volt <= 60) {
                                batLowRight.setVisibility(View.INVISIBLE);
                                batMiddleRight.setVisibility(View.VISIBLE);
                                batHighRight.setVisibility(View.INVISIBLE);
                            } else if (r_volt > 60 && r_volt <= 100) {
                                batLowRight.setVisibility(View.INVISIBLE);
                                batMiddleRight.setVisibility(View.INVISIBLE);
                                batHighRight.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                };
                Timer = new Timer();
                Timer.schedule(Task,0,10000);


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