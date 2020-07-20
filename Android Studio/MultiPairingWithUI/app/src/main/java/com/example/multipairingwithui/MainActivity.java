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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int LEFT =0;
    public static final int RIGHT =1;
    public static final int BOTH =100;
    String LEFT_STATE="LEFT_STATE";
    String RIGHT_STATE="RIGHT_STATE";
    int left_onoff=0;
    int  right_onoff=0;
    @SuppressLint("StaticFieldLeak")
    public static Context mainContext;
    private Messenger mServiceMessenger = null;
    boolean isService = false;

    ImageView leftRock;
    ImageView leftPaper;
    ImageView rightRock;
    ImageView rightPaper;

    //--------Right Hand---------
    Button reconnectRight;
    TextView bluetoothStateRight;

    //-------Left Hand----------
    Button reconnectLeft;
    TextView bluetoothStateLeft;

    BluetoothAdapter BA;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("create","여기는 크리에이트");
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
                        right_onoff=0;
                        break;
                    case bluetoothService.CONNECTED:
                        bluetoothStateRight.setText("오른손 연결됨");
                        rightRock.setVisibility(View.INVISIBLE);
                        rightPaper.setVisibility(View.VISIBLE);
                        right_onoff=1;
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
                        left_onoff=0;
                        break;
                    case bluetoothService.CONNECTING:
                        bluetoothStateLeft.setText("왼손 연결중");
                        break;
                    case bluetoothService.CONNECTED:
                        bluetoothStateLeft.setText("왼손 연결됨");
                        leftRock.setVisibility(View.INVISIBLE);
                        leftPaper.setVisibility(View.VISIBLE);
                        left_onoff=1;
                        break;
                }
            }
            else if(msg.what == BOTH)
            {
                leftRock.setVisibility(View.INVISIBLE);
                rightRock.setVisibility(View.INVISIBLE);
                leftPaper.setVisibility(View.INVISIBLE);
                rightPaper.setVisibility(View.INVISIBLE);
                bluetoothStateLeft.setVisibility(View.INVISIBLE);
                bluetoothStateRight.setVisibility(View.INVISIBLE);
                reconnectLeft.setVisibility(View.INVISIBLE);
                reconnectRight.setVisibility(View.INVISIBLE);

                //수화하세요 이모티콘 올리기
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