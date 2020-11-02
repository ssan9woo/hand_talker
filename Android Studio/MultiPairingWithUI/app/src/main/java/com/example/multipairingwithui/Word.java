package com.example.multipairingwithui;

import android.util.Log;

import androidx.annotation.NonNull;


public class Word implements Cloneable{
    String word;
    int[] right_flex;
    double[] right_gyro;
    boolean[] touch;
    int[] left_flex;
    double[] left_gyro;
    Word(){
        word="";
        right_flex=new int[6];
        right_gyro=new double[3];
        touch=new boolean[2];

        left_flex=new int[5];
        left_gyro=new double[3];
    }

    Word(String word){
        this.word=word;
        right_flex=new int[6];
        right_gyro=new double[3];
        touch=new boolean[2];

        left_flex=new int[5];
        left_gyro=new double[3];
    }

    public void set_flex(int[] left_flex, int[] right_flex){
        System.arraycopy(left_flex,0,this.left_flex,0,left_flex.length);
        System.arraycopy(right_flex,0,this.right_flex,0,right_flex.length);
    }
    public void set_gyro(double[] left_gyro, double[] right_gyro){
        System.arraycopy(left_gyro,0,this.left_gyro,0,left_gyro.length);
        System.arraycopy(right_gyro,0,this.right_gyro,0,right_gyro.length);
    }
    public void set_flex(int[] right_flex){
        System.arraycopy(right_flex,0,this.right_flex,0,right_flex.length);
    }
    public void set_gyro(double[] right_gyro){
        System.arraycopy(right_gyro,0,this.right_gyro,0,right_gyro.length);
    }
    public void set_touch(boolean[] touch){
        System.arraycopy(touch,0,this.touch,0,touch.length);
    }
    public int[] get_left_flex(){
        return left_flex;
    }
    public double[] get_left_gyro(){
        return left_gyro;
    }
    public int[] get_right_flex(){
        return right_flex;
    }
    public double[] get_right_gyro(){ return right_gyro; }
    public boolean[] get_touch(){
        return touch;
    }


//    public double getEuclideanDistance_flex(Word obj){
//        double dist=0.00;
//
//        for(int i=0;i<right_flex.length;i++){
//            dist += Math.pow(this.right_flex[i] - obj.right_flex[i], 2);
//        }
//
//        for(int i=0;i<left_flex.length;i++){
//            dist += Math.pow(this.left_flex[i] - obj.left_flex[i], 2);
//        }
//        return dist;
//    }
//    public double getEuclideanDistance_gyro(Word obj){
//        double dist=0.00;
//        for(int i=0;i<right_gyro.length-1;i++){
//            dist+= Math.pow(this.right_gyro[i] - obj.right_gyro[i],2);
//        }
//        for(int i=0;i<left_gyro.length-1;i++){
//            dist+= Math.pow(this.left_gyro[i] - obj.left_gyro[i],2);
//        }
//        return dist;
//    }
    public double getEuclideanDistance_flex(Word obj){
        double dist=0.00;

        for(int i=0;i<right_flex.length;i++){
            dist += Math.pow(this.right_flex[i] - obj.right_flex[i], 2);
        }
        Log.d("Dist", String.valueOf(dist)+obj.word+this.word);
        return dist;
    }
    public double getEuclideanDistance_gyro(Word obj){
        double dist=0.00;
        for(int i=0;i<right_gyro.length-1;i++){
            dist+= Math.pow(this.right_gyro[i] - obj.right_gyro[i],2);
        }
        return dist;
    }
    @Override
    @NonNull
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
