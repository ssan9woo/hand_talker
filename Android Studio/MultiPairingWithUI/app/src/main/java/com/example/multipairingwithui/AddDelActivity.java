package com.example.multipairingwithui;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddDelActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    //Variables
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    public static final String[] str_CONSONANT=new String[]{"ㄱ","ㄴ","ㄷ","ㄹ","ㅁ","ㅂ","ㅅ","ㅇ","ㅈ","ㅊ","ㅋ","ㅌ","ㅍ","ㅎ"};
    public static final String[] str_VOWEL=new String[]{"ㅏ","ㅑ","ㅓ","ㅕ","ㅗ","ㅛ","ㅜ","ㅠ","ㅡ","ㅣ"};
    public static final String consonant="CONSONANT";
    public static final String vowel="VOWEL";
    public static final String word="WORD";
    Button consonant_btnAdd, consonant_btnDel, vowel_btnAdd, vowel_btnDel, word_btnAdd, word_btnDel;
    EditText consonant_edittext,vowel_edittext,word_edittext;
    List<String> list_consonant = new ArrayList<>();
    List<String> list_vowel = new ArrayList<>();
    List<String> list_word = new ArrayList<>();
    ArrayAdapter<String> consonant_adapter;
    ArrayAdapter<String> vowel_adapter;
    ArrayAdapter<String> word_adapter;
    ListView consonant_listview;
    ListView vowel_listview;
    ListView word_listview;

    Context mContext;
    private boolean isService;
    private Messenger mServiceMessenger = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adddel);
        mContext=this;
        bindService(new Intent(AddDelActivity.this,bluetoothService.class), conn, Context.BIND_AUTO_CREATE);
        for(int i=0;i <str_CONSONANT.length;i++){
            if(PreferenceManager.IskeyinPref(consonant+str_CONSONANT[i],mContext)){
                list_consonant.add(str_CONSONANT[i]);
            }
        }
        for(int i=0;i<str_VOWEL.length;i++){
            if(PreferenceManager.IskeyinPref(vowel+str_VOWEL[i],mContext)){
                list_vowel.add(str_VOWEL[i]);
            }
        }
        String[] strlist = PreferenceManager.getWordList(mContext);
        if(strlist.length>0){
            list_word.addAll(Arrays.asList(strlist));
        }

        setLayout();
        isService = true;

        String SHARE_NAME = "SHARE_PREF";
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem( R.id.nav_add_del );

    }



    public void setLayout(){
        consonant_listview = (ListView) findViewById(R.id.consonant_listview);
        consonant_btnAdd = (Button) findViewById(R.id.consonant_btnAdd);
        consonant_btnDel = (Button) findViewById(R.id.consonant_btnDel);
        consonant_edittext =  (EditText) findViewById(R.id.consonant_edit);
        consonant_adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice, list_consonant);
        consonant_btnAdd.setOnClickListener(this);
        consonant_btnDel.setOnClickListener(this);
        consonant_listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        consonant_listview.setAdapter(consonant_adapter);

        vowel_listview = (ListView) findViewById(R.id.vowel_listview);
        vowel_btnAdd = (Button) findViewById(R.id.vowel_btnAdd);
        vowel_btnDel = (Button) findViewById(R.id.vowel_btnDel);
        vowel_edittext =  (EditText) findViewById(R.id.vowel_edit);
        vowel_adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice, list_vowel);
        vowel_btnAdd.setOnClickListener(this);
        vowel_btnDel.setOnClickListener(this);
        vowel_listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        vowel_listview.setAdapter(vowel_adapter);

        word_listview = (ListView) findViewById(R.id.word_listview);
        word_btnAdd = (Button) findViewById(R.id.word_btnAdd);
        word_btnDel = (Button) findViewById(R.id.word_btnDel);
        word_edittext =  (EditText) findViewById(R.id.word_edit);
        word_adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice, list_word);
        word_btnAdd.setOnClickListener(this);
        word_btnDel.setOnClickListener(this);
        word_listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        word_listview.setAdapter(word_adapter);

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        if(v == consonant_btnAdd){
            String str = consonant_edittext.getText().toString();
            if(str.length() != 0){
                consonant_edittext.setText("");
                list_consonant.add(str);
                PreferenceManager.save_syllable_value(bluetoothService.right_hand,bluetoothService.CONSONANT +str,mContext);
                consonant_adapter.notifyDataSetChanged();
            }
        }else if(v == consonant_btnDel){
            int pos = consonant_listview.getCheckedItemPosition();
            String str = consonant_adapter.getItem(pos);
            if(pos != ListView.INVALID_POSITION){
                list_consonant.remove(pos);
                consonant_listview.clearChoices();
                PreferenceManager.remove_syllable_value(bluetoothService.CONSONANT +str,mContext);
                consonant_adapter.notifyDataSetChanged();

            }
        }else if(v == vowel_btnAdd){
            String str = vowel_edittext.getText().toString();
            if(str.length() != 0){
                vowel_edittext.setText("");
                list_vowel.add(str);
                PreferenceManager.save_syllable_value(bluetoothService.right_hand,bluetoothService.VOWEL+str,mContext);
                vowel_adapter.notifyDataSetChanged();
            }
        }else if(v == vowel_btnDel){
            int pos = vowel_listview.getCheckedItemPosition();
            String str = vowel_adapter.getItem(pos);
            if(pos != ListView.INVALID_POSITION){
                list_vowel.remove(pos);
                vowel_listview.clearChoices();
                PreferenceManager.remove_syllable_value(bluetoothService.VOWEL+str,mContext);
                vowel_adapter.notifyDataSetChanged();

            }
        }else if(v == word_btnAdd){
            String str = word_edittext.getText().toString();
            if(str.length() != 0){
                word_edittext.setText("");
                list_word.add(str);
                PreferenceManager.save_word_value(bluetoothService.left_hand,str,mContext);
                PreferenceManager.save_word_value(bluetoothService.right_hand,str,mContext);
                word_adapter.notifyDataSetChanged();
            }
        }else if(v == word_btnDel){
            int pos = word_listview.getCheckedItemPosition();
            String str = word_adapter.getItem(pos);
            if(pos != ListView.INVALID_POSITION){
                list_word.remove(pos);
                word_listview.clearChoices();
                PreferenceManager.remove_word_value(str,mContext);
                word_adapter.notifyDataSetChanged();

            }
        }
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
    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            return false;
        }
    }));
    public void onDestory(){
        if(isService)
        {
            isService = false;
        }
        super.onDestroy();

    }
    @Override
    protected void onStop() {
        super.onStop();
        unbindService(conn);
    };
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch ( menuItem.getItemId() ){
            case R.id.nav_home:
                Intent intent_home = new Intent( AddDelActivity.this, MainActivity.class);
                startActivity( intent_home );
                break;
            case R.id.nav_scaling:
                Intent intent_setting = new Intent( AddDelActivity.this, scalingActivity.class);
                startActivity(intent_setting);
                break;
            case R.id.nav_add_del:
                break;
        }
        drawerLayout.closeDrawer( GravityCompat.START );
        finish();
        return true;
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
}
