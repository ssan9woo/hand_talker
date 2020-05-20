package com.example.multipairingwithui;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.MissingFormatArgumentException;
import java.util.Objects;

public class scalingActivity extends AppCompatActivity {
    Button button;
    TextView text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scaling);
        button = (Button)findViewById(R.id.button);
        text = (TextView)findViewById(R.id.textView);




        button.setOnClickListener(new View.OnClickListener() {
            Intent IntentExtras = getIntent();

            @Override
            public void onClick(View v)
            {
                String s = "";
                s = ((MainActivity) MainActivity.mainContext).sendFlexData();
                text.setText(s);
                //onNewIntent(IntentExtras);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        String uid = getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE);
        if(uid != null)
        {
            text.setText(uid);
        }
        else{
            text.setText("아직 null값");
        }
    }
}
