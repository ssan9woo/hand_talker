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
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "first";
    public static Context mainContext;
    private Messenger mServiceMessenger = null;
    boolean isService = false;

    TextView sangwoo;
    Button next;


    //--------Right Hand---------
    Button reconnectRight;

    TextView bluetoothStateRight;
    TextView rightEulerX;
    TextView rightEulerY;
    TextView rightEulerZ;

    TextView rightAccX;
    TextView rightAccY;
    TextView rightAccZ;

    //-------Left Hand----------
    Button reconnectLeft;

    TextView bluetoothStateLeft;
    TextView leftEulerX;
    TextView leftEulerY;
    TextView leftEulerZ;

    TextView leftAccX;
    TextView leftAccY;
    TextView leftAccZ;

    BluetoothAdapter BA;
    public static String str;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BA = BluetoothAdapter.getDefaultAdapter();

        if(!BA.isEnabled()){
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(i,5000);
        }

        sangwoo = (TextView)findViewById(R.id.sangwoo);
        //----------------------Find VIEW---------------------------------//

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
        next = (Button)findViewById(R.id.next);

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

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextIntent = new Intent(MainActivity.this,scalingActivity.class);
                startActivity(nextIntent);
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
            } catch (RemoteException e) {
            }
            Toast.makeText(getApplicationContext(), "Service Connected", Toast.LENGTH_LONG).show();
            isService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isService = false;
        }
    };

    public String sendFlexData(){
        //Intent scalingIntent = new Intent(MainActivity.this,scalingActivity.class);
        String s = rightEulerX.getText().toString();

        //scalingIntent.putExtra(EXTRA_MESSAGE,s);
        return s;
    }

    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {

        @SuppressLint("SetTextI18n")
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == 0) {
                switch(msg.arg1)
                {
                    case bluetoothService.DISCONNECT:
                        bluetoothStateRight.setText("오른손 연결끊김");
                        break;
                    case bluetoothService.CONNECTED:
                        bluetoothStateRight.setText("오른손 연결됨");
                        break;
                    case bluetoothService.CONNECTING:
                        bluetoothStateRight.setText("오른손 연결중");
                        break;
                    case bluetoothService.INPUTDATA:
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
            else if(msg.what == 1)
            {
                switch(msg.arg1)
                {
                    case bluetoothService.DISCONNECT:
                        bluetoothStateLeft.setText("왼손 연결끊김");
                        break;
                    case bluetoothService.CONNECTING:
                        bluetoothStateLeft.setText("왼손 연결중");
                        break;
                    case bluetoothService.CONNECTED:
                        bluetoothStateLeft.setText("왼손 연결됨");
                        break;
                    case bluetoothService.INPUTDATA:
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