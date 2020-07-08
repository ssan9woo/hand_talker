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

    private Messenger mClient = null;
    static sign mThread= null;
    boolean IsConnect_right = false,
            IsConnect_left = false;

    BluetoothAdapter BA;
    BluetoothDevice B_right,B_left;

    ConnectThread BC_right;
    ConnectThread BC_left;

    final String BMA_left = "00:18:91:D8:36:42" ;//Bluetooth0 MacAddress 자두이노 왼쪽
    final String BMA_right =  "00:18:E4:34:D4:8B"; //Bluetooth1 MacAddress 자두이노 오른쪽

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

    public void disconnectRight(){
        // Message msg = Message.obtain(null,0,DISCONNECT);
        if(BC_right != null)
        {
            try {
                BC_right.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void disconnectLeft(){
        // Message msg = Message.obtain(null,1,DISCONNECT);
        if(BC_left != null)
        {
            try {
                BC_left.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(){
        BA = BluetoothAdapter.getDefaultAdapter();
        B_right = BA.getRemoteDevice(BMA_right);
        B_left = BA.getRemoteDevice(BMA_left);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mReceiver,filter);
        mContext = this;
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
        if(!IsConnect_right)
        {
        BC_right = new ConnectThread(B_right,0);
        BC_right.start();
        }
    }
    public void reconnectLeft(){
        if(!IsConnect_left)
        {
        BC_left = new ConnectThread(B_left,1);
        BC_left.start();
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("BT SERVICE", "SERVICE STARTED");
        if(!IsConnect_right)
        {
            BC_right = new ConnectThread(B_right,0);
            BC_right.start();
        }
        if(!IsConnect_left)
        {
            BC_left = new ConnectThread(B_left,1);
            BC_left.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy(){

        if(BC_right != null)
        {
            try {
                BC_right.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(BC_left != null)
        {
            try {
                BC_left.cancel();
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
                if(!IsConnect_right)
                {
                    msg.what = bluetooth_index;
                    msg.arg1 = CONNECTING;
                    sendMsgToActivity(msg);
                }
                else if(!IsConnect_left)
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
        ArrayList<String> Data = new ArrayList<String>();

        public ConnectedThread(BluetoothSocket bluetoothsocket, int index) {
            bluetooth_index = index;
            try {
                in = bluetoothsocket.getInputStream();
                is = true;
                if(bluetooth_index == 0) IsConnect_right = is;
                else IsConnect_left = is;
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

                    /*
                    Left data format
                    X: 0.00, Y: 0.00, Z: 0.00, AccX: 0.00, AccY: 0.00, AccZ: 0.00,
                    Flex1: 3000, Flex2: 3000, Flex3: 3000, Flex4: 3000 , Flex5: 3000
                    왼쪽 손 총 최소 길이 = 54

                    Right data format
                    X: 0.00, Y: 0.00, Z: 0.00, AccX: 0.00, AccY: 0.00, AccZ: 0.00,
                    Flex1: 3000, Flex2: 3000, Flex3: 3000, Flex4: 3000 , Flex5: 3000, Flex6: 3000,
                    Capacitive Sensor1: 0/1, Capacitive Sensor2: 0/1
                    오른쪽 손 총 최소 길이 = 63
                    */

                    //System.out.println(s);
                    if(IsConnect_left && IsConnect_right){
                        if((bluetooth_index==0 && s.length()>=63) || (bluetooth_index==1 && s.length()>=54)) {
                            if(Data.size() < 5){
                                Data.add(s);
                            }
                            if(Data.size()==5) {
                                for (int i = 0; i < Data.size(); i++) {
                                    Message msg = Message.obtain(null, bluetooth_index, Data.get(i));
                                    mThread.bringHandler.sendMessage(msg);
                                }
                                Data.clear();
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

            if(bluetooth_index == 0) IsConnect_right = is;
            else IsConnect_left = is;

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

        double[] lastCoordinate_right = new double[]{0.00, 0.00, 0.00};
        double[] rightHand_Gyro = new double[]{0.00, 0.00, 0.00};
        double[] rightHand_Acc = new double[]{0.00, 0.00, 0.00};
        int[] rightHand_Flex = new int[]{0, 0, 0, 0, 0, 0,};
        boolean[] rightHand_Touch=new boolean[]{false, false};

        double[] lastCoordinate_left = new double[]{0.00, 0.00, 0.00};
        double[] leftHand_Gyro = new double[]{0.00, 0.00, 0.00};
        double[] leftHand_Acc = new double[]{0.00, 0.00, 0.00};
        int[] leftHand_Flex = new int[]{0, 0, 0, 0, 0, 0};
        double E_right=0;
        double E_left=0;
        double E_right_sum=0;
        double E_left_sum=0;
        Deque<Double> deq_right = new ArrayDeque<>(5);
        Deque<Double> deq_left = new ArrayDeque<>(5);

        sign(Handler handler){
            sendHandler = handler;
        }

        @SuppressLint("HandlerLeak")
        @Override
        public void run() {
            Looper.prepare();
            bringHandler = new Handler(){
                public void handleMessage(Message msg){
                    String[] arr = ((String)msg.obj).split(",");
                    //System.out.println(arr.length);
                    switch (msg.what){
                        case 0://Right hand
                            for(int i=0; i< 14;i++){
                                if(i > 11){
                                    rightHand_Touch[i-12] = Boolean.parseBoolean(arr[i]);
                                }
                                else if(i > 5){
                                    rightHand_Flex[i-6] = Integer.parseInt(arr[i]);
                                }
                                else if(i > 2){
                                    rightHand_Acc[i-3] = Double.parseDouble(arr[i]) * 5.0;
                                }
                                else{
                                    rightHand_Gyro[i] = Double.parseDouble(arr[i]);
                                }
                            }
                            E_right=get_energy(rightHand_Acc,lastCoordinate_right);
                            for(int i=0; i<3;i++)
                                lastCoordinate_right=rightHand_Acc;
                            deq_right.push(E_right);
                            E_right_sum+=E_right;
                            if(deq_right.size()>5)
                                E_right_sum-=deq_right.pollFirst();
                            break;
                        case 1://Left hand
                            for(int i=0; i< arr.length;i++){
                                if(i>5){
                                    leftHand_Flex[i-6] = Integer.parseInt(arr[i]);
                                }
                                else if(i>2){
                                    leftHand_Acc[i-3] = Double.parseDouble(arr[i]) * 5.0;
                                }
                                else{
                                    leftHand_Gyro[i] = Double.parseDouble(arr[i]);
                                }
                            }
                            E_left=get_energy(leftHand_Acc,lastCoordinate_left);
                            for(int i=0; i<3;i++)
                                lastCoordinate_left=leftHand_Acc;
                            deq_left.push(E_left);
                            E_left_sum+=E_left;
                            if(deq_left.size()>5)
                                E_left_sum-=deq_left.pollFirst();
                            break;
                        default:
                            break;
                }
                System.out.print(E_left_sum);
                System.out.print(" ");
                System.out.println(E_right_sum);
                /*
                if(E_left_sum>150& E_right_sum >150.0){
                }

                }*/

            };
            Looper.loop();
        }
        public double get_energy(double Acc[], double lastAcc[]){
            double energy;
            energy = Math.pow(Acc[0] - lastAcc[0],2) + Math.pow(Acc[1] - lastAcc[1],2) + Math.pow(Acc[2] - lastAcc[2],2) ;
            return energy;
        }
    }
}