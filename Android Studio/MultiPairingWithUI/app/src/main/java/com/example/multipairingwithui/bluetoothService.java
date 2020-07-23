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
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

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
    private Messenger mClient = null;
    static sign mThread= null;
    boolean IsConnect_right = false,
            IsConnect_left = false;
    static public int[] rightHand_Flex = new int[]{0, 0, 0, 0, 0, 0};
    static public int[] leftHand_Flex = new int[]{0, 0, 0, 0, 0};
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
    public static final String LEN_PREFIX = "Count_";
    public static final String VAL_PREFIX = "IntValue_";
    public static final int DISCONNECT = 0;
    public static final int CONNECTING = 50;
    public static final int CONNECTED = 2;
    public static final int INPUTDATA = 9999;
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_SEND_TO_ACTIVITY = 4;
    static SharedPreferences sharePref = null;
    static SharedPreferences.Editor editor = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClient = msg.replyTo;
                    break;
            }
            return false;
        }
    }));
    private void sendMsgToActivity(Message msg){
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

        BA = BluetoothAdapter.getDefaultAdapter();
        B_right = BA.getRemoteDevice(BMA_right);
        B_left = BA.getRemoteDevice(BMA_left);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mReceiver,filter);

        String SHARE_NAME = "SHARE_PREF";
        sharePref = getSharedPreferences(SHARE_NAME,MODE_PRIVATE);
        editor =sharePref.edit();

        mContext = this;
        mThread = new sign(mHandler);
        //mThread.setDaemon(true);
        mThread.start();

        user = new User();

        user.Set_min(getUserdata(str_hand[LEFT]+str_rock_or_paper[ROCK]),str_hand[LEFT]);
        user.Set_max(getUserdata(str_hand[LEFT]+str_rock_or_paper[PAPER]),str_hand[LEFT]);
        user.Set_min(getUserdata(str_hand[RIGHT]+str_rock_or_paper[ROCK]),str_hand[RIGHT]);
        user.Set_max(getUserdata(str_hand[RIGHT]+str_rock_or_paper[PAPER]),str_hand[RIGHT]);

        BC_right = new ConnectThread(B_right,RIGHT);
        BC_right.start();
        BC_left = new ConnectThread(B_left,LEFT);
        BC_left.start();
        super.onCreate();
    }
    public int getVcc(){
        int a = 0;
        return a;
    }
    public int[] getUserdata(String name){
        int[] ret;
        int count = sharePref.getInt(LEN_PREFIX + name, 0);
        ret = new int[count];
        for (int i = 0; i < count; i++){
            ret[i] = sharePref.getInt(VAL_PREFIX+ name + i, i);
        }
        return ret;
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

            try {
                BC_right.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                BC_left.cancel();
            } catch (IOException e) {
                e.printStackTrace();
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
                connectedThread.start();
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
        double[] rightHand_Gyro = new double[]{0.00, 0.00, 0.00};
        double[] rightHand_Acc = new double[]{0.00, 0.00, 0.00};

        boolean[] rightHand_Touch=new boolean[]{false, false};

        double[] lastCoordinate_left = new double[]{0.00, 0.00, 0.00};
        double[] leftHand_Gyro = new double[]{0.00, 0.00, 0.00};
        double[] leftHand_Acc = new double[]{0.00, 0.00, 0.00};

        double E_right=0.0;
        double E_left=0;
        Double E_right_sum=0.0;
        Double E_left_sum=0.0;
        double battery_left=0;
        double battery_right=0;
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
                    String[] arr = ((String)msg.obj).split(",");
                    switch (msg.what){
                        case RIGHT://Right hand
                            for(int i=0; i< arr.length;i++){
                                if(i > 13){
                                    battery_right=Double.parseDouble(arr[i]);
                                }
                                else if(i > 11){
                                    rightHand_Touch[i-12] = Boolean.parseBoolean(arr[i]);
                                }
                                else if(i > 5){
                                    rightHand_Flex[i-6] = user.Get_scaled_data(Integer.parseInt(arr[i]),i-6,"RIGHT");
                                }
                                else if(i > 2){
                                    rightHand_Acc[i-3] = Double.parseDouble(arr[i]) * 5.0;
                                }
                                else{
                                    rightHand_Gyro[i] = Double.parseDouble(arr[i]);
                                }
                            }
                            E_right=get_energy(RIGHT);
                            E_right_sum+=E_right;
                            rightenergy_q.offer(E_right);
                            if (rightenergy_q.size()>5) E_right_sum-=rightenergy_q.poll();

                            //Log.d("right energy",String.valueOf(E_right_sum));

                            if(E_left_sum >=150 || E_right_sum >= 150) {
                                Log.d("rightEnergy", String.valueOf(E_right_sum));
                                right_stack.push(rightHand_Gyro,rightHand_Flex);
                            }
                            break;
                        case LEFT://Left hand
                            for(int i=0; i< arr.length;i++){
                                if(i>10){
                                    battery_left=Double.parseDouble(arr[i]);
                                }
                                else if(i>5){
                                    leftHand_Flex[i-6] = user.Get_scaled_data(Integer.parseInt(arr[i]),i-6,"LEFT");
                                }
                                else if(i>2){
                                    leftHand_Acc[i-3] = Double.parseDouble(arr[i]) * 5.0;
                                }
                                else{
                                    leftHand_Gyro[i] = Double.parseDouble(arr[i]);
                                }
                            }
                            E_left=get_energy(LEFT);
                            E_left_sum+=E_left;
                            leftenergy_q.offer(E_left);
                            if (leftenergy_q.size()>5) E_left_sum-=leftenergy_q.poll();

                            if(E_left_sum >= 150 || E_right_sum >= 150) {
                                Log.d("leftEnergy", String.valueOf(E_left_sum));
                                left_stack.push(leftHand_Gyro,leftHand_Flex);
                            }
                            break;
                        default:
                            break;
                    }
                    //왼손 오른손 둘중 하나의 에너지가 150이상 이라면 스택에 쌓아둠//
                    if(E_left_sum < 150){
                        if(left_stack.IsGesture()){
                            Log.d("gesture",Arrays.toString(left_stack.popflex()) +" "+ Arrays.toString(left_stack.popgyro())+" "+ E_left_sum);
                        }
                        left_stack.clear();
                    }
                }
            };
            Looper.loop();
        }
        int get_energy(int hand){
            double energy;
            if(hand==RIGHT) {
                //energy = Math.pow(rightHand_Acc[0] - lastCoordinate_right[0], 2) + Math.pow(rightHand_Acc[1] - lastCoordinate_right[1], 2) + Math.pow(rightHand_Acc[2] - lastCoordinate_right[2], 2);
                energy = Math.pow(rightHand_Acc[0] , 2) + Math.pow(rightHand_Acc[1] , 2) + Math.pow(rightHand_Acc[2], 2);
                System.arraycopy(rightHand_Acc,0,lastCoordinate_right,0,rightHand_Acc.length);
            }
            else{
                //energy = Math.pow(leftHand_Acc[0] - lastCoordinate_left[0], 2) + Math.pow(leftHand_Acc[1] - lastCoordinate_left[1], 2) + Math.pow(leftHand_Acc[2] - lastCoordinate_left[2], 2);
                energy = Math.pow(leftHand_Acc[0] , 2) + Math.pow(leftHand_Acc[1], 2) + Math.pow(leftHand_Acc[2], 2);
                System.arraycopy(leftHand_Acc,0,lastCoordinate_left,0,leftHand_Acc.length);
            }
            return (int)Math.round(energy);
        }
    }
    public int[] getRightHand_Flex(){
        return rightHand_Flex;
    }
    public int[] getLeftHand_Flex(){
        return leftHand_Flex;
    }
    public static class Duration{
        Stack<int[]> flex;
        Stack<double[]> gyro;
        Duration(){
            flex = new Stack<>();
            gyro = new Stack<>();
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
        public void clear(){
            flex.clear();
            gyro.clear();
        }
        public boolean IsGesture(){
            //return !(20 > flex.size() || flex.size() > 150);
            return flex.size()>7;
        }
    }
}