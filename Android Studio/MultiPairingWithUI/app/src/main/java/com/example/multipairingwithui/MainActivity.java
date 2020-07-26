package com.example.multipairingwithui;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import static android.speech.tts.TextToSpeech.ERROR;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int BOTH = 100;
    public static final int GESTURE = 1000;
    public static final int SYLLABLE = 2000;
    public static final int CONSONANT = 2001;
    public static final int VOWEL = 2002;
    public static final int WORD = 3000;
    public static boolean isconnect_left = false;
    public static boolean isconnect_right = false;
    int arr_cnt=0;
    @SuppressLint("StaticFieldLeak")
    public static Context mainContext;
    private Messenger mServiceMessenger = null;
    boolean isService = false;
    private TextToSpeech tts;
    ImageView leftRock;
    ImageView leftPaper;
    ImageView rightRock;
    ImageView rightPaper;
    ImageView signImage;

    //--------Right Hand---------
    Button reconnectRight;
    TextView bluetoothStateRight;

    //-------Left Hand----------
    Button reconnectLeft;
    TextView bluetoothStateLeft;

    //animation
    Animation fadeOutAnimation;
    Animation fadeInAnimation;
    Animation clearAnimation;
    TextView signMessage;

    //Variables
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    BluetoothAdapter BA;

    Syllable[] consonants;
    Syllable[] vowels;

    @SuppressLint({"SetTextI18n", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BA = BluetoothAdapter.getDefaultAdapter();
        if (!BA.isEnabled()) {
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(i, 5000);
        }
        leftRock = findViewById(R.id.leftRock);
        leftPaper = findViewById(R.id.leftPaper);
        rightRock = findViewById(R.id.rightRock);
        rightPaper = findViewById(R.id.rightPaper);
        signImage = findViewById(R.id.signImage);
        signImage.setVisibility(View.INVISIBLE);
        signMessage = findViewById(R.id.signMessage);
        signMessage.setVisibility(View.INVISIBLE);
        leftPaper.setVisibility(View.INVISIBLE);
        rightPaper.setVisibility(View.INVISIBLE);
        reconnectRight = findViewById(R.id.reconnectRight);
        reconnectLeft = findViewById(R.id.reconnectLeft);
        bluetoothStateRight = findViewById(R.id.bluetoothStateRight);
        bluetoothStateLeft = findViewById(R.id.bluetoothStateLeft);
        if (savedInstanceState != null) {
            isconnect_left = savedInstanceState.getBoolean(bluetoothService.str_hand[LEFT]);
            isconnect_right = savedInstanceState.getBoolean(bluetoothService.str_hand[RIGHT]);
        }
        if(isconnect_left) {
            leftPaper.setVisibility(View.VISIBLE);
            leftRock.setVisibility(View.INVISIBLE);
            bluetoothStateLeft.setVisibility(View.INVISIBLE);
            reconnectLeft.setVisibility(View.INVISIBLE);
        }
        if(isconnect_right){
            rightPaper.setVisibility(View.VISIBLE);
            rightRock.setVisibility(View.INVISIBLE);
            bluetoothStateRight.setVisibility(View.INVISIBLE);
            reconnectRight.setVisibility(View.INVISIBLE);
        }



        mainContext = this;
        reconnectRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((bluetoothService) bluetoothService.mContext).reconnectRight();
            }
        });

        reconnectLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((bluetoothService) bluetoothService.mContext).reconnectLeft();
            }
        });

        for (int i = 0; i < AddDelActivity.str_CONSONANT.length; i++) {
            if (PreferenceManager.IskeyinPref(AddDelActivity.consonant + AddDelActivity.str_CONSONANT[i], mainContext)) {
                arr_cnt += 1;
            }
        }
        consonants = new Syllable[arr_cnt];
        arr_cnt = 0;
        for (int i = 0; i < AddDelActivity.str_VOWEL.length; i++) {
            if (PreferenceManager.IskeyinPref(AddDelActivity.vowel + AddDelActivity.str_VOWEL[i], mainContext)) {
                arr_cnt += 1;
            }
        }
        vowels = new Syllable[arr_cnt];

        for (int i = 0; i < AddDelActivity.str_CONSONANT.length; i++) {
            if (PreferenceManager.IskeyinPref(AddDelActivity.consonant + AddDelActivity.str_CONSONANT[i], mainContext)) {
                consonants[i] = new Syllable();
                try {
                    consonants[i] = (Syllable) PreferenceManager.get_gesture_value(AddDelActivity.consonant, AddDelActivity.str_CONSONANT[i], mainContext).clone();
                    Log.d("Oncreate", consonants[i].syllable + Arrays.toString(consonants[i].getFlex()) + Arrays.toString(consonants[i].getGyro())+Arrays.toString(consonants[i].getTouch()));
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }

        for (int i = 0; i < AddDelActivity.str_VOWEL.length; i++) {
            if (PreferenceManager.IskeyinPref(AddDelActivity.vowel + AddDelActivity.str_VOWEL[i], mainContext)) {
                vowels[i] = new Syllable();
                try {
                    vowels[i] = (Syllable) PreferenceManager.get_gesture_value(AddDelActivity.vowel, AddDelActivity.str_VOWEL[i], mainContext).clone();
                    Log.d("Oncreate", vowels[i].syllable + Arrays.toString(vowels[i].getFlex()) + Arrays.toString(vowels[i].getGyro())+Arrays.toString(vowels[i].getTouch()));
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }


        fadeOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadeout);
        fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
        clearAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.clear);

        /*-----------------------------------Hooks-------------------------------------*/
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        /*--------------------------Tool Bar----------------------------*/
        setSupportActionBar(toolbar);

        /*--------------------------Navigation Drawer Menu----------------------------*/

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });
        tts.setSpeechRate(1.0f);    // 읽는 속도는 기본 설정
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState); // 반드시 호출해 주세요.
        Log.d("onSaveInstanceState","여기는 세이브");
        // 추가로 자료를 저장하는 코드는 여기에 작성 하세요.
        outState.putBoolean(bluetoothService.str_hand[LEFT], isconnect_left);
        outState.putBoolean(bluetoothService.str_hand[RIGHT], isconnect_right);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(" onRestoreInstanceState","여기는 가져옴");
        // 추가로 자료를 복원하는 코드는 여기에 작성하세요.
    }
    @Override
    public void onBackPressed() {

        if( drawerLayout.isDrawerOpen( GravityCompat.START ) ){
            drawerLayout.closeDrawer( GravityCompat.START );
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch ( menuItem.getItemId() ){
            case R.id.nav_home:
                break;
            case R.id.nav_add_del:
                Intent intent_add = new Intent( MainActivity.this, AddDelActivity.class);
                startActivity(intent_add);
                break;
            case R.id.nav_scaling:
                Intent intent_scaling = new Intent( MainActivity.this, scalingActivity.class);
                startActivity(intent_scaling);
                break;
        }
        drawerLayout.closeDrawer( GravityCompat.START );
        //finish();
        return true;
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
        if(tts != null){
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        System.out.println("123123123");
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

    //초기상태 [왼손 상태 -> 왼손 연결중, Handler로 5초뒤에 연결됨 아닐시 연결 안된것으로 판단, 연결끊킴으로 넘어가기]
    //방법1. 5초뒤에 연결됨 아닐시에 service에서 sendMessage하는 함수 호출
    //sendMessage -> mainActivity로 disconnect 보내기
    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == RIGHT) {
                switch(msg.arg1)
                {
                    case bluetoothService.DISCONNECT:
                        bluetoothStateRight.setVisibility(View.VISIBLE);
                        bluetoothStateRight.setText("오른손 연결끊김");
                        rightPaper.setVisibility(View.INVISIBLE);
                        rightRock.setVisibility(View.VISIBLE);
                        reconnectRight.setVisibility(View.VISIBLE);
                        isconnect_right=false;
                        break;
                    case bluetoothService.CONNECTED:
                        bluetoothStateRight.setText("오른손 연결됨");
                        reconnectRight.setVisibility(View.INVISIBLE);
                        rightRock.setVisibility(View.INVISIBLE);
                        rightPaper.setVisibility(View.VISIBLE);
                        isconnect_right=true;
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
                        bluetoothStateLeft.setVisibility(View.VISIBLE);
                        bluetoothStateLeft.setText("왼손 연결끊김");
                        leftPaper.setVisibility(View.INVISIBLE);
                        leftRock.setVisibility(View.VISIBLE);
                        reconnectLeft.setVisibility(View.VISIBLE);
                        isconnect_left=false;
                        break;
                    case bluetoothService.CONNECTED:
                        bluetoothStateLeft.setText("왼손 연결됨");
                        reconnectLeft.setVisibility(View.INVISIBLE);
                        leftRock.setVisibility(View.INVISIBLE);
                        leftPaper.setVisibility(View.VISIBLE);
                        isconnect_left=true;
                        break;
                    case bluetoothService.CONNECTING:
                        bluetoothStateLeft.setText("왼손 연결중");
                        break;
                }
            }
            else if(msg.what == BOTH)
            {
                bluetoothStateLeft.setVisibility(View.INVISIBLE);
                bluetoothStateRight.setVisibility(View.INVISIBLE);
                reconnectLeft.setVisibility(View.INVISIBLE);
                reconnectRight.setVisibility(View.INVISIBLE);

                //animation
                rightPaper.startAnimation(fadeOutAnimation);
                leftPaper.startAnimation(fadeOutAnimation);
                signImage.startAnimation(fadeInAnimation);

                Handler mHandler = new Handler();
                mHandler.postDelayed(new Runnable()  {
                    public void run() {
                        rightPaper.startAnimation(clearAnimation);
                        leftPaper.startAnimation(clearAnimation);
                        signImage.startAnimation(fadeOutAnimation);
                        signMessage.startAnimation(fadeOutAnimation);
                    }
                }, 500); // 0.5초후
            }
            else if (msg.what == GESTURE){
                switch (msg.arg1){
                    /*객체 처리 해줘야함
                    msg.obj를 Syllable 객체로 만들어줘야함
                    Syllable 클래스안에
                    getEuclideanDistance()함수 사용
                    */
                    case SYLLABLE:
                        Syllable syllable;
                        syllable = (Syllable) msg.obj;
                        HashMap<String, Double> map = new HashMap<String, Double>();

                        for (Syllable consonant : consonants) {
                            if(Arrays.equals(consonant.touch,syllable.touch)) {
                                if (consonant.getEuclideanDistance_Flex(syllable) < 40) {
                                    map.put(consonant.syllable, consonant.getEuclideanDistance_Gyro(syllable));
                                    Log.d("Euclidean", consonant.syllable + " " + consonant.getEuclideanDistance_Gyro(syllable));
                                }
                            }
                        }
                        for(Syllable vowel : vowels){
                            if(Arrays.equals(vowel.touch,syllable.touch)) {
                                if (vowel.getEuclideanDistance_Flex(syllable) < 40) {
                                    map.put(vowel.syllable, vowel.getEuclideanDistance_Gyro(syllable));
                                    Log.d("Euclidean", vowel.syllable + " " + vowel.getEuclideanDistance_Gyro(syllable));
                                }
                            }
                        }
                        System.out.println(map.size()+String.valueOf(map.isEmpty()));

                        if(!map.isEmpty()) {
                            Log.d("map", String.valueOf(map));
                            String ret = HashMapSort(map);
                            Log.d("ret", ret);
                            tts.speak(ret, TextToSpeech.QUEUE_FLUSH, null);
                        }
                        break;
                    case WORD:
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
    public static String HashMapSort(final HashMap<String,Double> map) {
        List<String> list = new ArrayList<>(map.keySet());
        Collections.sort(list,new Comparator() {
            public int compare(Object o1,Object o2) {
                Object v1 = map.get(o1);
                Object v2 = map.get(o2);
                return ((Comparable) v2).compareTo(v1);
            }
        });
        //Collections.reverse(list);
        Log.d("After",String.valueOf(list));
        return list.get(list.size()-1);
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