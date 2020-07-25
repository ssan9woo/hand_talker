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
    public void setFlex(int[] flex){
            System.arraycopy(flex,0,this.flex,0,flex.length);
    }
    public void setGyro(double[] gyro){
        System.arraycopy(gyro,0,this.gyro,0,gyro.length);
    }
    public void setAcc(double[] acc){
        System.arraycopy(acc,0,this.acc,0,acc.length);
    }
    public void setTouch(boolean[] touch){
        System.arraycopy(touch,0,this.touch,0,touch.length);
    }
    public void setBattery(double battery){
        this.battery=battery;
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
