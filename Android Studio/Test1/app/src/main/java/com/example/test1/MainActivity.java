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

import java.util.Arrays;

public class MainActivity extends AppCompatActivity{

    Button button;
    TextView value;

    public void onCreate(Bundle saveInstacneState){
        super.onCreate(saveInstacneState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        value = (TextView) findViewById(R.id.value);

        final String[][] str = new String[1][2];
        str[0][0] = "sangwoo";
        str[0][1] = "hihi~";

        final sign mThread;
        mThread = new sign(mHandler);
        mThread.setDaemon(true);
        mThread.start();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = Message.obtain(null,1, Arrays.deepToString(str));
                mThread.backHandler.sendMessage(msg);
                Toast.makeText(getApplicationContext(),"상우" + Arrays.deepToString(str) ,Toast.LENGTH_LONG).show();
            }
        });
    }
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler(){
        String s = "";
        public void handleMessage(Message msg){
            switch(msg.what){
                case 1:
                    s = msg.obj.toString();
                    break;
            }
            value.setText(s);
        }
    };


    public class sign extends Thread{
        Handler backHandler;
        Handler mainHandler;
        String s = "";

        sign(Handler handler){
            mainHandler = handler;
        }
        @SuppressLint("HandlerLeak")
        public void run(){
            Looper.prepare();
            backHandler = new Handler(){
                public void handleMessage(Message msg){
                    switch(msg.what){
                        case 1:
                            s = msg.obj.toString();
                            break;
                    }
                    Message message = Message.obtain(null,1,s);
                    mainHandler.sendMessage(message);
                    //Message message2 = Message.obtain(null,0,s.substring(2,))
                }
            };
            Looper.loop();
        }
    }
}