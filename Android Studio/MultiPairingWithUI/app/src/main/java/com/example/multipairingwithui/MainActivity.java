package com.example.multipairingwithui;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    boolean flag = false;

    TextView sangwoo;


    //--------Right Hand---------
    Button connectRightButton;
    Button reconnectRight;

    TextView bluetoothStateRight;
    TextView rightEulerX;
    TextView rightEulerY;
    TextView rightEulerZ;

    TextView rightAccX;
    TextView rightAccY;
    TextView rightAccZ;

    //-------Left Hand----------
    Button connectLeftButton;
    Button reconnectLeft;

    TextView bluetoothStateLeft;
    TextView leftEulerX;
    TextView leftEulerY;
    TextView leftEulerZ;

    TextView leftAccX;
    TextView leftAccY;
    TextView leftAccZ;



    boolean IsConnect0 = false, IsConnect1 = false;

    BluetoothAdapter BA;
    BluetoothDevice B0,B1;

    ConnectThread BC0;
    ConnectThread BC1;

    final String B0MA = "98:D3:71:FD:47:5A"; //Bluetooth0 MacAddress
    final String B1MA = "98:D3:51:FD:88:9A"; //Bluetooth1 MacAddress

    final String SPP_UUID_STRING = "00001101-0000-1000-8000-00805F9B34FB"; //SPP UUID
    final UUID SPP_UUID = UUID.fromString(SPP_UUID_STRING);

    final int DISCONNECT = 0;
    final int CONNECTING = 1;
    final int CONNECTED = 2;
    final int INPUTDATA = 9999;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sangwoo = (TextView)findViewById(R.id.sangwoo);
        //----------------------Find VIEW---------------------------------//
        connectRightButton = (Button)findViewById(R.id.connectRightButton);
        connectLeftButton = (Button)findViewById(R.id.connectLeftButton);
        reconnectRight = (Button)findViewById(R.id.reconnectRight);
        reconnectLeft = (Button)findViewById(R.id.reconnectLeft);

        bluetoothStateRight = (TextView)findViewById(R.id.bluetoothStateRight);
        bluetoothStateLeft = (TextView)findViewById(R.id.bluetoothStateLeft);

        rightEulerX = (TextView)findViewById(R.id.rightEulerX);
        rightEulerY = (TextView)findViewById(R.id.rightEulerY);
        rightEulerZ = (TextView)findViewById(R.id.rightEulerZ);
        rightAccX = (TextView)findViewById(R.id.rightAccX);
        rightAccY = (TextView)findViewById(R.id.rightAccY);
        rightAccZ = (TextView)findViewById(R.id.rightAccZ);


        leftEulerX = (TextView)findViewById(R.id.leftEulerX);
        leftEulerY = (TextView)findViewById(R.id.leftEulerY);
        leftEulerZ = (TextView)findViewById(R.id.leftEulerZ);
        leftAccX = (TextView)findViewById(R.id.leftAccX);
        leftAccY = (TextView)findViewById(R.id.leftAccY);
        leftAccZ = (TextView)findViewById(R.id.leftAccZ);

        rightEulerX.setText("rightEuler x");
        rightEulerY.setText("rightEuler y");
        rightEulerZ.setText("rightEuler z");
        rightAccX.setText("rightAcc x");
        rightAccY.setText("rightAcc y");
        rightAccZ.setText("rightAcc z");

        leftEulerX.setText("leftEuler x");
        leftEulerY.setText("leftEuler y");
        leftEulerZ.setText("leftEuler z");
        leftAccX.setText("leftAcc x");
        leftAccY.setText("leftAcc y");
        leftAccZ.setText("leftAcc z");
        sangwoo.setText("");

        //----------------------SET Listener---------------------------------//
        connectRightButton.setOnClickListener(this);
        connectLeftButton.setOnClickListener(this);

        //----------------------Bluetooth init---------------------------------//

        BA = BluetoothAdapter.getDefaultAdapter();

        if(!BA.isEnabled()){
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(i,5000);
        }

        B0 = BA.getRemoteDevice(B0MA);
        B1 = BA.getRemoteDevice(B1MA);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mainBroadcastReceiver,filter);



        reconnectRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    BC0.cancel();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Handler delay = new Handler();
                delay.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(!IsConnect0)
                        {
                            BC0 = new ConnectThread(B0,0);
                            BC0.start();
                        }
                    }
                },100);
            }
        });

        reconnectLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    BC1.cancel();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Handler delay = new Handler();
                delay.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(!IsConnect1)
                        {
                            BC1 = new ConnectThread(B1,1);
                            BC1.start();
                        }
                    }
                },100);
            }
        });

    }

    public void onStart() {
        super.onStart();
        if(!IsConnect0)
        {
            BC0 = new ConnectThread(B0,0);
            BC0.start();
        }
        if(!IsConnect1)
        {
            BC1 = new ConnectThread(B1,1);
            BC1.start();
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

    //Bluetooth state -> View Change
    Handler handler = new Handler(new Handler.Callback() {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what == 0){      //오른손
                switch (msg.arg1){
                    case DISCONNECT:
                        IsConnect0 = false;
                        connectRightButton.setText("CONNECT");
                        bluetoothStateRight.setText("오른손 연결끊김");
                        break;
                    case CONNECTING:
                        bluetoothStateRight.setText("오른손 연결중");
                        break;
                    case CONNECTED:
                        IsConnect0 = true;
                        connectRightButton.setEnabled(true);
                        connectRightButton.setText("DISCONNECT");
                        bluetoothStateRight.setText("오른손 연결됨");
                        break;
                    case 10:
                        sangwoo.setText("R");
                        break;
                    case INPUTDATA:
                        String s = (String)msg.obj;
                        String[] arr = new String[6];
                        for(int i = 0 ; i < 6; i ++)
                        {
                            arr[i] = "";
                        }
                        int j = 0;
                        for(int i = 0; i< s.length(); i++)
                        {
                            if(s.charAt(i) == ',')
                            {
                                j += 1;
                                continue;
                            }
                            else
                            {
                                arr[j] += s.charAt(i);
                            }
                        }
                        rightEulerX.setText("euler x :".concat(arr[0]));
                        rightEulerY.setText("euler y :".concat(arr[1]));
                        rightEulerZ.setText("euler z :".concat(arr[2]));
                        rightAccX.setText("acc x :".concat(arr[3]));
                        rightAccY.setText("acc y :".concat(arr[4]));
                        rightAccZ.setText("acc z :".concat(arr[5]));
                        break;
                }

            }
            else if(msg.what == 1){     //왼손
                switch (msg.arg1){
                    case DISCONNECT:
                        IsConnect1 = false;
                        connectLeftButton.setText("CONNECT");
                        bluetoothStateLeft.setText("왼손 연결끊김");
                        break;
                    case CONNECTING:
                        bluetoothStateLeft.setText("왼손 연결중");
                        break;
                    case CONNECTED:
                        IsConnect1 = true;
                        connectLeftButton.setEnabled(true);
                        connectLeftButton.setText("DISCONNECT");
                        bluetoothStateLeft.setText("왼손 연결됨");
                        break;
                    case 10:
                        sangwoo.setText("L");
                        break;
                    case INPUTDATA:
                        String s = (String)msg.obj;

                        String[] arr = new String[6];
                        for(int i = 0 ; i < 6; i ++)
                        {
                            arr[i] = "";
                        }
                        int j = 0;
                        for(int i = 0; i< s.length(); i++)
                        {
                            if(s.charAt(i) == ',')
                            {
                                j += 1;
                                continue;
                            }
                            else
                            {
                                arr[j] += s.charAt(i);
                            }
                        }
                        leftEulerX.setText("euler x :".concat(arr[0]));
                        leftEulerY.setText("euler y :".concat(arr[1]));
                        leftEulerZ.setText("euler z :".concat(arr[2]));
                        leftAccX.setText("acc x :".concat(arr[3]));
                        leftAccY.setText("acc y :".concat(arr[4]));
                        leftAccZ.setText("acc z :".concat(arr[5]));
                        break;
                }
            }
            return true;
        }
    });

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.connectRightButton){
            if(IsConnect0){
                //블루투스 연결된 상태
                if(BC0 != null){
                    try {
                        BC0.cancel();

                        Message m = new Message();
                        m.what = 0;
                        m.arg1 = DISCONNECT;
                        handler.sendMessage(m);

                        BC0 = null;
                    } catch (IOException e) { }
                }
            }
            else {
                //블루투스 끈어진 상태
                v.setEnabled(false);
                BC0 = new ConnectThread(B0,0);
                BC0.start();

            }
        }

        else{
            if(IsConnect1){
                //블루투스 연결된 상태
                if(BC1 != null){
                    try {
                        BC1.cancel();

                        Message m = new Message();
                        m.what = 1;
                        m.arg1 = DISCONNECT;
                        handler.sendMessage(m);

                        BC1 = null;
                    } catch (IOException e) { }
                }
            }else{
                //블루투스 끈어진
                v.setEnabled(false);
                BC1 = new ConnectThread(B1,1);
                BC1.start();
            }
        }
    }

    //connect bluetooth
    class ConnectThread extends Thread{

        BluetoothDevice BD;
        BluetoothSocket BS;

        int bluetooth_index;

        ConnectedThread connectedThread;

        ConnectThread(BluetoothDevice device , int index){
            BD = device;
            bluetooth_index = index;
        }

        @Override
        public void run() {
            try {
                sendMessage(CONNECTING);

                BS = BD.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
                BS.connect();

                connectedThread = new ConnectedThread(BS, bluetooth_index);
                connectedThread.start();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    cancel();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                if(connectedThread != null){
                    connectedThread.cancel();
                }
            }
        }

        public void cancel() throws IOException {

            if(connectedThread != null){
                connectedThread.cancel();
            }
            if(BS != null)
            {
                try{
                    BS.close();
                    BS = null;
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
            sendMessage(DISCONNECT);
        }

        public void sendMessage(int arg){
            Message m = new Message();
            m.what = bluetooth_index;
            m.arg1 = CONNECTING;

            handler.sendMessage(m);
        }
    }

    //connected bluetooth - communication
    class ConnectedThread extends Thread{

        InputStream in = null;

        int bluetooth_index;

        boolean is =false;

        public ConnectedThread(BluetoothSocket bluetoothsocket, int index) {
            bluetooth_index = index;

            try {
                in = bluetoothsocket.getInputStream();

                is = true;

                if(bluetooth_index == 0) IsConnect0 = is;
                else IsConnect1 = is;

                sendMessage(CONNECTED);

            } catch (IOException e) {
                cancel();
            }
        }

        @Override
        public void run() {
            BufferedReader Buffer_in = new BufferedReader(new InputStreamReader(in));

            while (is){
                try {
                    String s = Buffer_in.readLine();

                    if(!s.equals("")){
                        sendMessage(INPUTDATA,s);
                        //여기다 왼손오른손 구분할게 필요.
                    }

                } catch (IOException e) { }
            }

        }

        public void sendMessage(int arg){
            Message m = new Message();

            m.what = bluetooth_index;
            m.arg1 = arg;

            handler.sendMessage(m);
        }

        public void sendMessage(int arg, String s){
            Message m = new Message();
            Message v = new Message();

            m.what = bluetooth_index;
            m.arg1 = arg;
            m.obj = s;

            handler.sendMessage(m);
            if(bluetooth_index == 0)
            {
                v.what = 0;
                v.arg1 = 10;
                handler.sendMessage(v);
            }
            else if(bluetooth_index == 1)
            {
                v.what = 1;
                v.arg1 = 10;
                handler.sendMessage(v);
            }
        }

        public void cancel(){
            is = false;

            if(bluetooth_index == 0) IsConnect0 = is;
            else IsConnect1 = is;

            if(in != null){
                try {
                    in.close();
                    in=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            sendMessage(DISCONNECT);
        }
    }
    public void onDestroy(){

        if(BC0 != null)
        {
            try {
                BC0.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(BC1 != null)
        {
            try {
                BC1.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        unregisterReceiver(mainBroadcastReceiver);

        super.onDestroy();
    }
    public BroadcastReceiver mainBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message isDisconnectedMessage = new Message();
            final String action = intent.getAction();
            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // what = 0, arg1 = DISCONNECT
                if(device.getName().equals("sign"))
                {
                    isDisconnectedMessage.what = 0;
                    isDisconnectedMessage.arg1 = DISCONNECT;
                    handler.sendMessage(isDisconnectedMessage);
                }

                else if(device.getName().equals("HC-06"))
                {
                    isDisconnectedMessage.what = 1;
                    isDisconnectedMessage.arg1 = DISCONNECT;
                    handler.sendMessage(isDisconnectedMessage);
                }
            }
        }
    };
}