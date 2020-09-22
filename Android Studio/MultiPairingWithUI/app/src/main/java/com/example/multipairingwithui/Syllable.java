package com.example.multipairingwithui;

import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.util.Log;

import androidx.annotation.NonNull;


public class Syllable implements Cloneable{
    String syllable;
    int[] flex;
    double[] gyro;
    boolean[] touch;

    Syllable(){
        syllable="";
        flex=new int[6];
        gyro=new double[3];
        touch=new boolean[2];
    }

    Syllable(String syllable){
        this.syllable=syllable;
        flex=new int[6];
        gyro=new double[3];
        touch=new boolean[2];
    }


    public void setFlex(int[] flex){
        System.arraycopy(flex,0,this.flex,0,flex.length);
    }
    public void setGyro(double[] gyro){
        System.arraycopy(gyro,0,this.gyro,0,gyro.length);
    }
    public void setTouch(boolean[] touch){
        System.arraycopy(touch,0,this.touch,0,touch.length);
    }
    public int[] getFlex(){
        return flex;
    }
    public double[] getGyro(){
        return gyro;
    }
    public boolean[] getTouch(){
        return touch;
    }
    public double getEuclideanDistance_Flex(Syllable obj){
        double dist=0.00;

        for(int i=0;i<flex.length;i++){
            if ( i== flex.length-1){
                dist+=Math.pow(this.flex[i] - obj.flex[i],2)*2;
            }
            else {
                dist += Math.pow(this.flex[i] - obj.flex[i], 2);
            }
        }
        return dist;
    }
    public double getEuclideanDistance_Gyro(Syllable obj){
        double dist=0.00;
        for(int i=0;i<gyro.length-1;i++){
            dist+= Math.pow(this.gyro[i] - obj.gyro[i],2);
        }
        return dist;
    }

    @Override
    @NonNull
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
