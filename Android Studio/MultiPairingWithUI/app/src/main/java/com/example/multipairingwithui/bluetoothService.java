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
import android.content.SharedPreferences;
import android.content.SyncAdapterType;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class bluetoothService extends Service {
    @SuppressLint("StaticFieldLeak")
    static public  Context mContext;
    bluetoothReceiver mReceiver;
    private static Messenger mClient = null;
    static sign mThread= null;
    boolean IsConnect_right = false,
            IsConnect_left = false;
    public static Hand left_hand;
    public static Hand right_hand;
    public static int[] left_rowflex= new int[]{0, 0, 0, 0, 0};
    public static int[] right_rowflex= new int[]{0, 0, 0, 0, 0, 0};
    BluetoothAdapter BA;
    BluetoothDevice B_right,B_left;
    public static User user;
    ConnectThread BC_right;
    ConnectThread BC_left;
    private Intent nextIntent;
    final String BMA_left = "00:18:91:D8:36:42" ;//Bluetooth0 MacAddress 자두이노 왼쪽
    final String BMA_right =  "00:18:E4:34:D4:8B"; //Bluetooth1 MacAddress 자두이노 오른쪽

    final String SPP_UUID_STRING = "00001101-0000-1000-8000-00805F9B34FB"; //SPP UUID

    final UUID SPP_UUID = UUID.fromString(SPP_UUID_STRING);

    public static final int LEFT =0;
    public static final int RIGHT =1;
    public static final int ROCK=0;
    public static final int PAPER=1;
    public static final int BOTH=100;
    public static final String[] str_hand={"LEFT","RIGHT"};
    public static final String[] str_rock_or_paper={"ROCK","PAPER"};
    public static final String CONSONANT ="CONSONANT";
    public static final String VOWEL="VOWEL";
    public static final String WORD="WORD";
    public static final int DISCONNECT = 0;
    public static final int CONNECTING = 50;
    public static final int CONNECTED = 2;
    public static final int INPUTDATA = 9999;
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_SEND_TO_ACTIVITY = 4;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_REGISTER_CLIENT) {
                mClient = msg.replyTo;
            }
            return false;
        }
    }));
    public static void sendMsgToActivity(Message msg){
        try{
            mClient.send(msg);
        } catch (NullPointerException | RemoteException ignored) {}
    }

    public void disconnectRight(){
        Message msg = Message.obtain(null,RIGHT,DISCONNECT);
        sendMsgToActivity(msg);
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
        Message msg = Message.obtain(null,LEFT,DISCONNECT);
        sendMsgToActivity(msg);
        if(BC_left != null)
        {
            try {
                BC_left.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    @SuppressLint("CommitPrefEdits")
    @Override
    public void onCreate(){
        super.onCreate();
        BA = BluetoothAdapter.getDefaultAdapter();
        B_right = BA.getRemoteDevice(BMA_right);
        B_left = BA.getRemoteDevice(BMA_left);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mReceiver,filter);

        mContext = this;
        mThread = new sign(mHandler);
        //mThread.setDaemon(true);
        mThread.start();

        user = new User();;
        try {
            user.Set_min(PreferenceManager.getUserdata((str_hand[RIGHT] + str_rock_or_paper[ROCK]),mContext), str_hand[RIGHT]);
            user.Set_max(PreferenceManager.getUserdata((str_hand[RIGHT] + str_rock_or_paper[PAPER]),mContext), str_hand[RIGHT]);
            user.Set_min(PreferenceManager.getUserdata((str_hand[LEFT] + str_rock_or_paper[ROCK]),mContext), str_hand[LEFT]);
            user.Set_max(PreferenceManager.getUserdata((str_hand[LEFT] + str_rock_or_paper[PAPER]),mContext), str_hand[LEFT]);
        }catch (Exception e){
            Log.e("ERROR","Could not load default values",e);
        }
        //Log.d("set", Arrays.toString(PreferenceManager.getUserdata((str_hand[LEFT] + str_rock_or_paper[ROCK]), mContext)));
        //Log.d("set", Arrays.toString(PreferenceManager.getUserdata((str_hand[LEFT] + str_rock_or_paper[PAPER]), mContext)));
        //Log.d("set", Arrays.toString(PreferenceManager.getUserdata((str_hand[RIGHT] + str_rock_or_paper[ROCK]), mContext)));
        //Log.d("set", Arrays.toString(PreferenceManager.getUserdata((str_hand[RIGHT] + str_rock_or_paper[PAPER]), mContext)));
        left_hand = new Hand(str_hand[LEFT]);
        right_hand= new Hand(str_hand[RIGHT]);
        super.onCreate();
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
        }
    };

    public void reconnectRight(){
        if(!IsConnect_right)
        {
            BC_right = new ConnectThread(B_right,RIGHT);
            BC_right.start();
        }
    }
    public void reconnectLeft(){
        if(!IsConnect_left)
        {
            BC_left = new ConnectThread(B_left,LEFT);
            BC_left.start();
        }
    }
    public void onDestroy(){
        if(mReceiver != null)
            unregisterReceiver(mReceiver);
        if(BC_right !=null) {
            try {
                BC_right.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(BC_left !=null) {
            try {
                BC_left.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onDestroy();
    }
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device)
            throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) ((Method) m).invoke(device, SPP_UUID);
        } catch (Exception e) {
            Log.e("ERROR", "Could not create Insecure RFComm Connection",e);
        }
        return device.createRfcommSocketToServiceRecord(SPP_UUID);
    }

    class ConnectThread extends Thread{

        BluetoothDevice BD;
        BluetoothSocket BS;

        int bluetooth_index;

        ConnectedThread connectedThread;

        ConnectThread(BluetoothDevice device , int index){
            bluetooth_index = index;
            try{
                //BS = BD.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
                BS=createBluetoothSocket(device);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            Message msg = new Message();
            msg.what = bluetooth_index;
            msg.arg1 = CONNECTING;
            sendMsgToActivity(msg);

            try {
                BS.connect();
                connectedThread = new ConnectedThread(BS, bluetooth_index);
                connectedThread.start();;

            } catch (IOException e) {
                e.printStackTrace();
                try {
                    BS.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                if(connectedThread != null){
                    connectedThread.cancel();
                }
            }
        }
        void cancel() throws IOException {
            if(connectedThread != null){
                connectedThread.cancel();
            }
            try{
                BS.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }
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
                if(bluetooth_index == RIGHT) IsConnect_right = is;
                else IsConnect_left = is;
                sendMessage(CONNECTED);
                if (IsConnect_left && IsConnect_right){
                    Message a = Message.obtain(null,BOTH);
                    sendMsgToActivity(a);
                }
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
<<<<<<< HEAD
                    Log.d("123",s);
                    /*
                    Left data format
                    X: 0.00, Y: 0.00, Z: 0.00, AccX: 0.00, AccY: 0.00, AccZ: 0.00,
                    Flex1: 3000, Flex2: 3000, Flex3: 3000, Flex4: 3000 , Flex5: 3000 , VCC: 0.00
                    왼쪽 손 총 최소 길이 = 77
                    Right data format
                    X: 0.00, Y: 0.00, Z: 0.00, AccX: 0.00, AccY: 0.00, AccZ: 0.00,
                    Flex1: 3000, Flex2: 3000, Flex3: 3000, Flex4: 3000 , Flex5: 3000, Flex6: 3000,
                    Capacitive Sensor1: 0/1, Capacitive Sensor2: 0/1, VCC:0.00
                    오른쪽 손 총 최소 길이 = 85
                    */

                    if(IsConnect_left || IsConnect_right){
                        if((bluetooth_index==LEFT && s.length()>=77) || (bluetooth_index==RIGHT && s.length()>=85)) {
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
                } catch (IOException ignored) { }
            }
        }

        void sendMessage(int arg){
            Message m = new Message();
            m.what = bluetooth_index;
            m.arg1 = arg;
            sendMsgToActivity(m);
        }

        void cancel(){
            is = false;

            if(bluetooth_index == RIGHT) IsConnect_right = false;
            else IsConnect_left = false;

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
        double[] lastCoordinate_left = new double[]{0.00, 0.00, 0.00};

        double[] Acc = new double[]{0, 0, 0};
        boolean[] Touch=new boolean[]{false, false};
        int[] Flex_left = new int[]{0, 0, 0, 0, 0};
        int[] Flex_right = new int[]{0, 0, 0, 0, 0, 0};
        double[] Gyro = new double[]{0, 0, 0};
        double battery=0;
        double E_right=0.0;
        double E_left=0;
        Double E_right_sum=0.0;
        Double E_left_sum=0.0;

        Queue<Double> leftenergy_q = new LinkedList<>();
        Queue<Double> rightenergy_q = new LinkedList<>();
        Duration left_stack;
        Duration right_stack;
        sign(Handler handler){
            sendHandler = handler;
            left_stack = new Duration();
            right_stack = new Duration();
        }
        @SuppressLint("HandlerLeak")
        @Override
        public void run() {
            Looper.prepare();
            bringHandler = new Handler(){
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                public void handleMessage(@NonNull Message msg){
                    String[] arr = {};
                    arr = ((String)msg.obj).split(",");

                    switch (msg.what){
                        case RIGHT://Right hand
                            for(int i=0; i< arr.length;i++){
                                if(i > 13){
                                    battery=Double.parseDouble(arr[i]);
                                }
                                else if(i > 11){
                                    Touch[i-12] = Boolean.parseBoolean(arr[i]);
                                }
                                else if(i > 5){
                                    right_rowflex[i - 6] = Integer.parseInt(arr[i]);
                                    Flex_right[i-6] = user.Get_scaled_data(Integer.parseInt(arr[i]),i-6,"RIGHT");
                                }
                                else if(i > 2){
                                    Acc[i-3] = Double.parseDouble(arr[i]) * 5.0;
                                }
                                else{
                                    Gyro[i] = Double.parseDouble(arr[i]);
                                }
                            }
                            right_hand.setBattery(battery);
                            right_hand.setAcc(Acc);
                            right_hand.setFlex(Flex_right);
                            right_hand.setGyro(Gyro);
                            right_hand.setTouch(Touch);

                            E_right=get_energy(RIGHT);
                            E_right_sum+=E_right;
                            rightenergy_q.offer(E_right);
                            if (rightenergy_q.size()>5) E_right_sum-=rightenergy_q.poll();
                            if(E_right_sum >= 130) {
                                right_stack.push(Gyro,Flex_right);
                            }
                            break;
                        case LEFT://Left hand
                            for(int i=0; i< arr.length;i++){
                                if(i>10){
                                    battery=Double.parseDouble(arr[i]);
                                }
                                else if(i>5){
                                    left_rowflex[i - 6] = Integer.parseInt(arr[i]);
                                    Flex_left[i-6] = user.Get_scaled_data(Integer.parseInt(arr[i]),i-6,"LEFT");

                                }
                                else if(i>2){
                                    Acc[i-3] = Double.parseDouble(arr[i]) * 5.0;
                                }
                                else{
                                    Gyro[i] = Double.parseDouble(arr[i]);
                                }
                            }
                            left_hand.setBattery(battery);
                            left_hand.setAcc(Acc);
                            left_hand.setFlex(Flex_left);
                            left_hand.setGyro(Gyro);
                            E_left=get_energy(LEFT);
                            E_left_sum+=E_left;
                            leftenergy_q.offer(E_left);
                            if (leftenergy_q.size()>5) E_left_sum-=leftenergy_q.poll();
                            if(E_left_sum >= 130) {
                                left_stack.push(Gyro,Flex_left);
                            }
                            break;
                    }
                    //왼손 오른손 둘중 하나의 에너지가 150이상 이라면 스택에 쌓아둠//
                    if(E_right_sum < 80 && E_left_sum < 80){
                        //얘도 다시설정 -> 지화의 끝구간이 너무 가속도 민감도가 높으면 동작이 끝나기도 전에 구간이 종료될 수 있음.
                        Message m = new Message();
                        m.what=MainActivity.GESTURE;
                        if(left_stack.IsGesture() && right_stack.IsGesture()){
                            Word word = new Word();
                            word.set_flex(left_stack.popflex(), right_stack.popflex());
                            word.set_gyro(left_stack.popgyro(), right_stack.popgyro());
                            word.set_touch(right_hand.getTouch());
                            m.arg1=MainActivity.WORD;
                            m.obj= word;
                        }
                        else if(right_stack.IsGesture()){
                            Syllable syllable =  new Syllable();
                            syllable.setFlex(right_stack.popflex());
                            syllable.setGyro(right_stack.popgyro());
                            syllable.setTouch(right_hand.getTouch());
                            m.arg1=MainActivity.SYLLABLE;
                            m.obj= syllable;
                        }
                        sendMsgToActivity(m);
                        left_stack.clear();
                        right_stack.clear();
                    }
                }
            };
            Looper.loop();
        }
        int get_energy(int hand){
            double energy;
            double[] acc = new double[]{0,0,0};
            if(hand==RIGHT) {
                System.arraycopy(right_hand.getAcc(),0,acc,0,acc.length);
                //energy = Math.pow(rightHand_Acc[0] - lastCoordinate_right[0], 2) + Math.pow(rightHand_Acc[1] - lastCoordinate_right[1], 2) + Math.pow(rightHand_Acc[2] - lastCoordinate_right[2], 2);
                energy = Math.pow(acc[0] , 2) + Math.pow(acc[1] , 2) + Math.pow(acc[2], 2);
                System.arraycopy(acc,0,lastCoordinate_right,0,acc.length);
            }
            else{
                System.arraycopy(left_hand.getAcc(),0,acc,0,acc.length);
                //energy = Math.pow(leftHand_Acc[0] - lastCoordinate_left[0], 2) + Math.pow(leftHand_Acc[1] - lastCoordinate_left[1], 2) + Math.pow(leftHand_Acc[2] - lastCoordinate_left[2], 2);
                energy = Math.pow(acc[0] , 2) + Math.pow(acc[1], 2) + Math.pow(acc[2], 2);
                System.arraycopy(acc,0,lastCoordinate_left,0,acc.length);
            }
            return (int)Math.round(energy);
        }
    }
//    public int[] getRaw_Flex_left(String hand){
//        if(hand==str_hand[LEFT])
//            return left_rowflex;
//        else
//            return right_rowflex;
//    }

    public static class Duration{
        Stack<int[]> flex;
        Stack<double[]> gyro;
        Stack<double[]> acc;
        Duration(){
            flex = new Stack<>();
            gyro = new Stack<>();
            acc = new Stack<>();
        }
        public void push(double[] _gyro,int[] _flex){
            flex.push(_flex);
            gyro.push(_gyro);
        }
        public int[] popflex(){
            return flex.pop();
        }
        public double[] popgyro(){
            return gyro.pop();
        }
        public  double[] popacc() { return acc.pop();}
        public void clear(){
            flex.clear();
            gyro.clear();
            acc.clear();
        }
        public boolean IsGesture(){
            return flex.size()>4;
        }
    }
}
