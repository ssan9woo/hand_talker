package com.example.multipairingwithui;

public class Hand {
    String hand;
    int[] flex;
    double[] gyro;
    double[] acc;
    boolean[] touch;
    double battery;
    Hand(String h){
        hand=h;
        gyro= new double[]{0.00,0.00,0.00};
        acc = new double[]{0.00,0.00,0.00};
        battery=3.00;
        if(h=="RIGHT") flex= new int[]{0, 0, 0, 0, 0, 0};
        else flex= new int[]{0, 0, 0, 0, 0};
        if(h=="RIGHT") touch=new boolean[]{false,false};
    }
    public void setFlex(int[] v){
            System.arraycopy(v,0,flex,0,v.length);
    }
    public void setGyro(double[] v){
        System.arraycopy(v,0,gyro,0,v.length);
    }
    public void setAcc(double[] v){
        System.arraycopy(v,0,acc,0,v.length);
    }
    public void setTouch(boolean[] v){
        System.arraycopy(v,0,touch,0,v.length);
    }
    public void setBattery(double v){
        battery=v;
    }
    public int[] getFlex(){
        return flex;
    }
    public double[] getGyro(){
        return  gyro;
    }
    public boolean[] getTouch(){
        return touch;
    }
    public int getDatalength(){
        if(hand == "RIGHT")
            return gyro.length+flex.length+touch.length;
        else
            return gyro.length+flex.length;
    }
    public double[] getAcc(){
        return  acc;
    }
    public double getBattery(){
        return battery;
    }
    public String getHand(){
        return hand;
    }
}
