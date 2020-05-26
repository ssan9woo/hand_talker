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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

public class bluetoothService extends Service {
    public static Context mContext;
    private BroadcastReceiver mReceiver;

    Deque<String> valuesX = new ArrayDeque<>(5);
    Deque<String> valuesY = new ArrayDeque<>(5);
    ArrayList<String[]> Data = new ArrayList<String[]>();

    boolean flag1 = false;
    boolean flag2 = false;

    private Messenger mClient = null;
    private Messenger mClient2 = null;
    //IBinder mBinder = new MyBinder();

    boolean IsConnect0 = false,
            IsConnect1 = false;

    BluetoothAdapter BA;
    BluetoothDevice B0,B1;

    ConnectThread BC0;
    ConnectThread BC1;

    final String B0MA = "98:D3:71:FD:47:5A"; //Bluetooth0 MacAddress
    final String B1MA = "98:D3:51:FD:88:9A"; //Bluetooth1 MacAddress

    final String SPP_UUID_STRING = "00001101-0000-1000-8000-00805F9B34FB"; //SPP UUID
    final UUID SPP_UUID = UUID.fromString(SPP_UUID_STRING);

    public static final int DISCONNECT = 0;
    public static final int CONNECTING = 50;
    public static final int CONNECTED = 2;
    public static final int INPUTDATA = 9999;



    // onCreate : adapter, 및 변수설정, onStartCommand : 연결, onBind : 송신,  left, right 함수 두개로 송신


//    public class MyBinder extends Binder{
//        bluetoothService getMyservice(){ return bluetoothService.this; }
//    }

    //////////////////////////////
    //onUnbind() 필요한지 직접 꺼봐야함.
    //////////////////////////////


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    //수신값(현재 필요없음)
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
    public void disconnectRight(){
        // Message msg = Message.obtain(null,0,DISCONNECT);
        if(BC0 != null)
        {
            try {
                BC0.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnectLeft(){
        // Message msg = Message.obtain(null,1,DISCONNECT);
        if(BC1 != null)
        {
            try {
                BC1.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(){
        BA = BluetoothAdapter.getDefaultAdapter();
        B0 = BA.getRemoteDevice(B0MA);
        B1 = BA.getRemoteDevice(B1MA);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mReceiver,filter);
        mContext = this;

        final sign mThread;
        mThread = new sign(mHandler);
        mThread.setDaemon(true);
        mThread.start();

        super.onCreate();
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case 1:
                    break;
            }
        }
    };

    public void reconnectRight(){
        //if(!IsConnect0)//
        //{
        BC0 = new ConnectThread(B0,0);
        BC0.start();
        //}
    }
    public void reconnectLeft(){
        //if(!IsConnect1)
        //{
        BC1 = new ConnectThread(B1,1);
        BC1.start();
        //}
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("BT SERVICE", "SERVICE STARTED");
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
        return super.onStartCommand(intent, flags, startId);
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
                if(!IsConnect0)
                {
                    msg.what = bluetooth_index;
                    msg.arg1 = CONNECTING;
                    sendMsgToActivity(msg);
                }
                else if(!IsConnect1)
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

            //handler.sendMessage(m);
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

            final sign signThread;

            signThread = new sign(mHandler);
            signThread.setDaemon(true);
            signThread.start();

            BufferedReader Buffer_in = new BufferedReader(new InputStreamReader(in));

            while (is){
                try {
                    String s = Buffer_in.readLine();
                    if(IsConnect1 && IsConnect0) {
                        if (!s.equals("")) {
                            sendMessage(INPUTDATA, s);

                            if(bluetooth_index == 0){
                                if(valuesX.size() < 5){
                                    valuesX.push(s);
                                    if(valuesX.size() == 5 && valuesY.size() == 5){
                                        //combine
                                        for(int i = 0; i < 5; i++){
                                            Data.add(new String[]{valuesX.pollFirst(),valuesY.pollFirst()});
                                            Message handData = Message.obtain(null,0,Data.get(i));
                                            signThread.bringHandler.sendMessage(handData);
                                        }
                                        Data.clear();
                                        valuesX.clear();
                                        valuesY.clear();
                                    }
                                }
                            }
                            else if(bluetooth_index == 1){
                                if(valuesY.size() < 5){
                                    valuesY.push(s);
                                    if(valuesY.size() == 5 && valuesX.size() == 5){
                                        //combine
                                        for(int i = 0; i < 5; i++){
                                            Data.add(new String[]{valuesX.pollFirst(),valuesY.pollFirst()});
                                            Message handData = Message.obtain(null,0,Data.get(i));
                                            signThread.bringHandler.sendMessage(handData);
                                        }
                                        Data.clear();
                                        valuesX.clear();
                                        valuesY.clear();
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

            if(bluetooth_index == 0) IsConnect0 = is;
            else IsConnect1 = is;

            if(in != null){
                try {
                    in.close();
                    in = null;
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
        ArrayList<String[]> handData;
        private final Double[] zeroCordinate = {0.00,0.00,0.00};
        Double[] lastCordinateX;


        public sign(Handler handler){
            sendHandler = handler;
            lastCordinateX = zeroCordinate;
        }

        @SuppressLint("HandlerLeak")
        @Override
        public void run() {
            Looper.prepare();
            bringHandler = new Handler(){
                public void handleMessage(Message msg){
                    switch (msg.what){
                        case 0:
                            String[] s = (String[]) msg.obj;
                            System.out.println("오른손 Data : " + s[0] + "   왼손 Data : " + s[1]);
                            /*
                            //2차원배열 오브젝트로 받음[[x1,y1],[x1,y1],[x1,y1],[x1,y1],[x1,y1]]
                            //앞에서부터 하나씩 받고 casting 후에 while문 들어가서 에너지 구하기.
                            //ebinm : 6, flex : 6, 접촉 : 2
                            //원소 나누고 -> scaling -> e
                            ArrayList<String[]> signData = (ArrayList<String[]>) msg.obj;
                            //signData.get(0)[0] -> 14개의
                            for(int i = 0; i < 5 ; i++){
                                String[] rightValues = signData.get(i)[0].split(",");
                                String[] leftValues = signData.get(i)[1].split(",");

                                Double accX = Double.parseDouble(rightValues[3]);
                                Double accY = Double.parseDouble(rightValues[4]);
                                Double accZ = Double.parseDouble(rightValues[5]);

                                Double E = Math.pow(accX - lastCordinateX[0],2) + Math.pow(accY - lastCordinateX[1],2) + Math.pow(accZ - lastCordinateX[2],2);

                                lastCordinateX[0] = accX;
                                lastCordinateX[1] = accY;
                                lastCordinateX[2] = accZ;

                                Message m = Message.obtain(null,0,E);
                                sendHandler.sendMessage(m);
                                //정수실수처리
                                //스케일링2
                                //시작을 원점 (0,0,0)
                                //E1 = (x1 - x0)^2 +
                            }
                            //E(total ) > 150
                            //while(조건) 조건 1,2,3
                            // 조건 1 : 지화검출 -> while(오른 > 150) -> 끝점
                            */
                            break;
                    }
                    //send
                }
            };
            Looper.loop();
        }
    }
}
