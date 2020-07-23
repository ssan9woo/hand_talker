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

public class MainActivity extends AppCompatActivity {

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

    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == RIGHT) {
                switch(msg.arg1)
                {
                    case bluetoothService.DISCONNECT:
                        bluetoothStateRight.setText("오른손 연결끊김");
                        rightRock.setVisibility(View.VISIBLE);
                        rightPaper.setVisibility(View.INVISIBLE);
                        reconnectRight.setVisibility(View.VISIBLE);
                        break;
                    case bluetoothService.CONNECTED:
                        bluetoothStateRight.setText("오른손 연결됨");
                        rightRock.setVisibility(View.INVISIBLE);
                        rightPaper.setImageResource(R.drawable.nnn);

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
                        leftRock.setVisibility(View.VISIBLE);
                        leftPaper.setVisibility(View.INVISIBLE);
                        reconnectLeft.setVisibility(View.VISIBLE);
                        break;
                    case bluetoothService.CONNECTING:
                        bluetoothStateLeft.setText("왼손 연결중");
                        break;
                    case bluetoothService.CONNECTED:
                        bluetoothStateLeft.setText("왼손 연결됨");
                        leftRock.setVisibility(View.INVISIBLE);
                        leftPaper.setImageResource(R.drawable.left);
                        leftPaper.setVisibility(View.VISIBLE);

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
                //딜레이
                Handler mHandler = new Handler();
                mHandler.postDelayed(new Runnable()  {
                    public void run() {
                        signImage.startAnimation(fadeOutAnimation);
                        signMessage.startAnimation(fadeOutAnimation);

                    }
                }, 3000); // 0.5초후

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