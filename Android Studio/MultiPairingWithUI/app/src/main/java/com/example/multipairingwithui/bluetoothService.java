package com.example.multipairingwithui;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

public class bluetoothService<signLanguageProcessThread> extends Service {
    public static Context mContext;
    private BroadcastReceiver mReceiver;

    private Messenger mClient = null;
    private Messenger mClient2 = null;
    //IBinder mBinder = new MyBinder();

    boolean IsConnect_left = false,
            IsConnect_Right = false;

    BluetoothAdapter BA;
    BluetoothDevice B_Left, B_Right;

    ConnectThread ConThrd_Left;
    ConnectThread ConThrd_Right;

    final String MAC_Left =  "00:18:E4:34:D4:8E";//Bluetooth0 MacAddress
    final String MAC_Right = "00:18:91:D8:36:42"; //Bluetooth1 MacAddress
    final String SPP_UUID_STRING = "00001101-0000-1000-8000-00805F9B34FB"; //SPP UUID
    final UUID SPP_UUID = UUID.fromString(SPP_UUID_STRING);

    public static final int DISCONNECT = 0;
    public static final int CONNECTING = 50;
    public static final int CONNECTED = 2;
    public static final int INPUTDATA = 9999;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }


    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch(msg.what){
                case 1:
                    mClient = msg.replyTo;
                    break;
            }
            return false;
        }
    }));

    private void sendMsgToActivity(Message msg){
        try{
            mClient.send(msg);
        } catch (NullPointerException | RemoteException e) {}
    }

    private void sendMsgToSubActivity(Message msg){

    }
    public void disconnectLeft(){
       // Message msg = Message.obtain(null,0,DISCONNECT);
        if(ConThrd_Left != null)
        {
            try {
                ConThrd_Left.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnectRight(){
       // Message msg = Message.obtain(null,1,DISCONNECT);
        if(ConThrd_Right != null)
        {
            try {
                ConThrd_Right.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(){
        BA = BluetoothAdapter.getDefaultAdapter();
        B_Left = BA.getRemoteDevice(MAC_Left);
        B_Right = BA.getRemoteDevice(MAC_Right);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mReceiver,filter);

        mContext = this;
        super.onCreate();
    }

    public void reconnectRight(){
        //if(!IsConnect0)//
        //{
        ConThrd_Right = new ConnectThread(B_Right,1);
        ConThrd_Right.start();
        //}
    }
    public void reconnectLeft(){
        //if(!IsConnect1)
        //{
        ConThrd_Left = new ConnectThread(B_Left,0);
        ConThrd_Left.start();
        //}
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("BT SERVICE", "SERVICE STARTED");
        if(!IsConnect_left)
        {
            ConThrd_Left = new ConnectThread(B_Left,0);
            ConThrd_Left.start();
        }
        if(!IsConnect_Right)
        {
            ConThrd_Right = new ConnectThread(B_Right,1);
            ConThrd_Right.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy(){

        if(ConThrd_Left != null)
        {
            try {
                ConThrd_Left.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(ConThrd_Right != null)
        {
            try {
                ConThrd_Right.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }


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
                Message msg = new Message();
                if(!IsConnect_left)
                {
                    msg.what = bluetooth_index;
                    msg.arg1 = CONNECTING;
                    sendMsgToActivity(msg);
                }
                else if(!IsConnect_Right)
                {
                    msg.what = bluetooth_index;
                    msg.arg1 = CONNECTING;
                    sendMsgToActivity(msg);
                }

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
        }

        public void sendMessage(int arg){
            Message m = new Message();
            m.what = bluetooth_index;
            m.arg1 = CONNECTING;

        }
    }

    //connected bluetooth - communication
    class ConnectedThread extends Thread{


        InputStream in = null;
        int bluetooth_index;
        boolean is =false;
        sign mThread;


        @SuppressLint("HandlerLeak")
        Handler mHandler = new Handler(){
            public void handleMessage(Message msg){
                switch (msg.what){
                    case 0:
                        break;
                }
            }
        };

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

            mThread = new sign(mHandler);
            mThread.setDaemon(true);
            mThread.start();
        }

        @Override
        public void run() {
            BufferedReader Buffer_in = new BufferedReader(new InputStreamReader(in));
            Deque<String> valuesX = new ArrayDeque<>(5);
            Deque<String> valuesY = new ArrayDeque<>(5);
            String[][] synchronizedString = new String[5][2];

            while (is){
                try {
                    String s = Buffer_in.readLine();
                    //if(IsConnect_Right && IsConnect_left) {
                        if (!s.equals("")) {
                            sendMessage(INPUTDATA, s);

                            if(bluetooth_index == 0){
                                if(valuesX.size() < 5){
                                    valuesX.push(s);
                                    if(valuesX.size() == 5 && valuesY.size() == 5){
                                        //combine
                                        for(int i = 0; i < 5; i++){
                                            synchronizedString[i][0] = valuesX.pollFirst();
                                            synchronizedString[i][1] = valuesY.pollFirst();
                                        }
                                        Message msg = Message.obtain(null,0, Arrays.deepToString(synchronizedString));
                                        mThread.bringHandler.sendMessage(msg);
                                    }
                                }
                                else {
                                    valuesX.pollFirst();
                                    valuesX.push(s);
                                    if(valuesY.size() == 5){
                                        //combine
                                        for(int i = 0; i < 5; i++){
                                            synchronizedString[i][0] = valuesX.pollFirst();
                                            synchronizedString[i][1] = valuesY.pollFirst();
                                        }
                                        Message msg = Message.obtain(null,0,Arrays.deepToString(synchronizedString));
                                        mThread.bringHandler.sendMessage(msg);
                                    }
                                }
                            }
                            else if(bluetooth_index == 1){
                                if(valuesY.size() < 5){
                                    valuesY.push(s);
                                    if(valuesY.size() == 5 && valuesX.size() == 5){
                                        //combine
                                        for(int i = 0; i < 5; i++){
                                            synchronizedString[i][0] = valuesX.pollFirst();
                                            synchronizedString[i][1] = valuesY.pollFirst();
                                        }
                                        Message msg = Message.obtain(null,0,Arrays.deepToString(synchronizedString));
                                        mThread.bringHandler.sendMessage(msg);
                                    }
                                }
                                else {
                                    valuesY.pollFirst();
                                    valuesY.push(s);
                                    if(valuesX.size() == 5){
                                        //combine
                                        for(int i = 0; i < 5; i++){
                                            synchronizedString[i][0] = valuesX.pollFirst();
                                            synchronizedString[i][1] = valuesY.pollFirst();
                                        }
                                        Message msg = Message.obtain(null,0,Arrays.deepToString(synchronizedString));
                                        mThread.bringHandler.sendMessage(msg);
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException e) { }
            }
        }

        public void sendMessage(int arg){
            Message m = new Message();

            m.what = bluetooth_index;
            m.arg1 = arg;

            sendMsgToActivity(m);
        }

        public void sendMessage(int arg, String s){
            Message m = new Message();
            //Message v = new Message();

            m.what = bluetooth_index;
            m.arg1 = arg;
            m.obj = s;
            sendMsgToActivity(m);

        }

        public void cancel(){
            is = false;

            if(bluetooth_index == 0) IsConnect_left = is;
            else IsConnect_Right = is;

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


    public static class sign extends Thread{
        Handler bringHandler;
        Handler sendHandler;
        String signData = "";
        public sign(Handler handler){
            sendHandler = handler;
        }

        @SuppressLint("HandlerLeak")
        @Override
        public void run() {
            Looper.prepare();
            bringHandler = new Handler(){
                public void handleMessage(Message msg){
                    switch (msg.what){
                        case 0:
                            //2차원배열 오브젝트로 받음[[x1,y1],[x1,y1],[x1,y1],[x1,y1],[x1,y1]]
                            signData = msg.obj.toString();
                            break;
                    }
                    //send
                }
            };
            Looper.loop();
        }
    }
}