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
            case "LEFT":
                System.arraycopy(value,0,left_min,0,left_min.length);
                left_min=value;
                break;
            case "RIGHT":
                System.arraycopy(value,0,right_min,0,right_min.length);
                right_min=value;
                break;
        }
    }
    public void Set_max(int value[],String hand){
        switch (hand){
            case "LEFT":
                System.arraycopy(value,0,left_max,0,left_max.length);
                left_max=value;
                break;
            case "RIGHT":
                System.arraycopy(value,0,right_max,0,right_max.length);
                right_max=value;
                break;
        }
    }
    public int Get_scaled_data(int flex_value,int idx,String hand){
        double ret=0.0;
        switch (hand) {
            case "LEFT":
                ret = (double) (flex_value - this.left_min[idx]) / (this.left_max[idx] - this.left_min[idx]);
                break;
            case "RIGHT":
                ret = (double) (flex_value - this.right_min[idx]) / (this.right_max[idx] - this.right_min[idx]);
                break;
        }
        return (int) Math.round(ret*10);
    }
}
