package com.example.multipairingwithui;

public class User {
    private int[] left_min;
    private int[] left_max;
    private int[] right_min;
    private int[] right_max;
    public User(){
        left_min = new int[5];
        left_max = new int[5];
        right_min = new int[6];
        right_max = new int[6];
    }
    public void Set_min(int value[],String hand){
        switch (hand){
            case "left":
                left_min=value;
                break;
            case "right":
                right_min=value;
                break;
        }
    }
    public void Set_max(int value[],String hand){
        switch (hand){
            case "left":
                left_max=value;
                break;
            case "right":
                right_max=value;
                break;
        }
    }

    public double Get_scaled_data(int value,int idx,String hand){
        double ret=0.00;
        switch (hand) {
            case "left":
                ret = (double) (value - this.left_min[idx]) / (this.left_max[idx] - this.left_min[idx]);
                break;
            case "right":
                ret = (double) (value - this.right_min[idx]) / (this.right_max[idx] - this.right_min[idx]);
                break;
        }
        return ret;
    }
}
