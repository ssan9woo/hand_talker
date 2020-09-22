package com.example.dtwtest;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int[] s = {1,2,3,4,5};
        int[] t = {1,2,7,8,9,10,11,12,13};
        System.out.println("DTW Distance : " + secondary_Classification(s,t));
    }

    public int Uclidian(int a, int b){
        return Math.abs(a - b);
    }
    //Dynamic Time Warping
    public int secondary_Classification(int[] stored_data, int[] input_data){
        final int MAX = 3000;
        int cost, stored_data_len = stored_data.length, input_data_len = input_data.length;
        int[][] dtw = new int[stored_data_len + 1][input_data_len + 1];
        //[1]
        for(int i = 0; i <= stored_data_len; i++){
            for(int j = 0; j <= input_data_len; j++){
                if(j == 0 && (i >= 1 && i <= stored_data_len))
                    dtw[i][0] = stored_data[i-1];
                else if(i == 0 && (j >= 1 && j <= input_data_len))
                    dtw[0][j] = input_data[j-1];
                else if (i == 0 && j == 0)
                    dtw[i][j] = 0;
                else
                    dtw[i][j] = MAX;
            }
        }
        //[2]
        for(int i = 1; i <= stored_data_len; i++){
            for(int j = 1 ; j <= input_data_len; j++){

                cost = Uclidian(dtw[i][0],dtw[0][j]);

                dtw[i][j] = cost + Math.min(dtw[i-1][j],Math.min(dtw[i][j-1],dtw[i-1][j-1]));

            }
        }
        //[3]
        return dtw[stored_data_len][input_data_len];
    }

    /*
   public int secondary_Classification(int[] stored_data, int[] input_data){
       final int MAX = 3000;
       int cost, stored_data_len = stored_data.length, input_data_len = input_data.length;
       int[][] dtw = new int[stored_data_len + 1][input_data_len + 1];

//[1]
       for(int i = 0; i <= stored_data_len; i++){
           for(int j = 0; j <= input_data_len; j++){
               if(j == 0 && i >= 1 && i <= stored_data_len)
                   dtw[i][0] = stored_data[i-1];
               else if(i == 0 && j >= 1 && j <= input_data_len)
                   dtw[0][j] = stored_data[j-1];
               else if (i == 0 && j == 0)
                   dtw[i][j] = 0;
               else
                   dtw[i][j] = MAX;
           }
       }
//[2]
       for(int i = 1; i <= stored_data_len; i++){
           for(int j = 1 ; j <= input_data_len; j++){
               cost = Uclidian(dtw[i][0],dtw[0][j]);
               dtw[i][j] = cost + Math.min(dtw[i-1][j],Math.min(dtw[i][j-1],dtw[i-1][j-1]));
           }
       }
//[3]
       return dtw[stored_data_len][input_data_len];
   }
   */


}