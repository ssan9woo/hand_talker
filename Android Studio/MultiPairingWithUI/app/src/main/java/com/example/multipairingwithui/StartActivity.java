package com.example.multipairingwithui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT= 1500;
    // Hooks
    View first, second, third, fourth, fifth, sixth;
    TextView hand, slogan;

    // Animations
    Animation topAnimation, bottomAnimation, middleAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView(R.layout.activity_start);

        topAnimation = AnimationUtils.loadAnimation(this, R.anim.top_animation );
        middleAnimation = AnimationUtils.loadAnimation(this, R.anim.middle_animation );
        bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation );

        //Hooks
        first = findViewById( R.id.first_line );
        second = findViewById( R.id.second_line );
        third = findViewById( R.id.third_line );
        fourth = findViewById( R.id.fourth_line );
        fifth = findViewById( R.id.fifth_line );
        sixth = findViewById( R.id.sixth_line );

        hand = findViewById( R.id.hand );
        slogan = findViewById( R.id.sign );

        first.setAnimation( topAnimation );
        second.setAnimation( topAnimation );
        third.setAnimation( topAnimation );
        fourth.setAnimation( topAnimation );
        fifth.setAnimation( topAnimation );
        sixth.setAnimation( topAnimation );

        hand.setAnimation( middleAnimation );
        slogan.setAnimation( bottomAnimation );

        //splash Screen
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(StartActivity.this, MainActivity.class );
                startActivity( intent );
                finish();
            }
        }, SPLASH_TIME_OUT );
    }
}
