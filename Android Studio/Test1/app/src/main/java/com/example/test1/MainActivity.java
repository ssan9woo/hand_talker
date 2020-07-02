package com.example.test1;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity{

    Button button;
    TextView value;

    public void onCreate(Bundle saveInstacneState){
        super.onCreate(saveInstacneState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        value = (TextView) findViewById(R.id.value);

        final String[][] str = new String[3][2];
        str[0][0] = "1";
        str[0][1] = "10";
        str[1][0] = "100";
        str[1][1] = "1000";
        str[2][0] = "10000";
        str[2][1] = "100000";



        final sign mThread;
        mThread = new sign(mHandler);
        mThread.setDaemon(true);
        mThread.start();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<String[]> signData = new ArrayList<String[]>();
                signData.add(new String[]{str[0][0],str[0][1]});
                signData.add(new String[]{str[1][0],str[1][1]});
                signData.add(new String[]{str[2][0],str[2][1]});

                //Toast.makeText(getApplicationContext(), Arrays.toString(signData.get(1)),Toast.LENGTH_LONG).show();
                Message msg = Message.obtain(null,1,signData);
                mThread.backHandler.sendMessage(msg);
            }
        });
    }
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler(){


        @SuppressLint("SetTextI18n")
        public void handleMessage(Message msg){

            switch(msg.what){
                case 1:
                    ArrayList<String[]> sign = (ArrayList<String[]>) msg.obj;
                    value.setText("x : " + sign.get(0)[0] + "y : " + sign.get(0)[1]);
//                    ArrayList signData = (ArrayList) msg.obj;
//                    String m = signData.get(0)[1];
//                    value.setText(m);
                    break;
            }
        }
    };


    public class sign extends Thread{
        Handler backHandler;
        Handler mainHandler;


        sign(Handler handler){
            mainHandler = handler;
        }
        @SuppressLint("HandlerLeak")
        public void run(){
            Looper.prepare();
            backHandler = new Handler(){
                public void handleMessage(Message msg) {
                    ArrayList<String[]> sign = new ArrayList<String[]>();
                    switch (msg.what) {
                        case 1:
                            sign = (ArrayList<String[]>) msg.obj;
                            String m = sign.get(0)[0];
                            Toast.makeText(getApplicationContext(),m,Toast.LENGTH_LONG).show();
                            break;
                    }
                    for(int i = 0; i < 3; i++){
                        Message message = Message.obtain(null, 1, sign);
                        mainHandler.sendMessage(message);
                    }
                }
            };
            Looper.loop();
        }
    }
}