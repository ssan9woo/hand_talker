package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;

import com.github.kimkevin.hangulparser.HangulParser;
import com.github.kimkevin.hangulparser.HangulParserException;

import static android.speech.tts.TextToSpeech.ERROR;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    String[] consonant;
    ArrayList<String> collection;
    ArrayList<String> fixedCollection;
    ArrayList<String> fixedConsonant;
    ArrayList<Integer> collection_index = new ArrayList<Integer>();
    ArrayList<String> hi = new ArrayList<>();
    private TextToSpeech tts;
    Button button;
    ArrayList<String> arr = new ArrayList<String>();
    ArrayList<Integer> num = new ArrayList<>();
    Handler h = new Handler();

    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        consonant = new String[]{"ㄱ","ㄴ","ㄷ","ㄹ","ㅁ","ㅂ","ㅅ","ㅇ","ㅈ","ㅊ","ㅋ","ㅌ","ㅍ","ㅎ"};
        collection = new ArrayList<String>(Arrays.asList("ㅏ","ㅐ","ㅑ","ㅒ","ㅓ","ㅔ","ㅕ","ㅖ","ㅗ","ㅘ","ㅙ","ㅚ","ㅛ","ㅜ","ㅝ","ㅞ","ㅟ","ㅠ","ㅡ","ㅢ","ㅣ"));
        fixedCollection = new ArrayList<String>(Arrays.asList("0","0","0","0","0","0","0","0","ㅘ","ㅙ","0","0","0","0","0","0","0","ㅝ","ㅞ"));
        fixedConsonant = new ArrayList<String>(Arrays.asList("0","0","0","0","0","0","0","0","ㄺ","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","ㄼ","ㄵ","0","0","0","0","0","ㄶ","0","0","0","0","ㅄ"));

        //arr.add("ㄷ");arr.add("ㅏ");arr.add("ㄹ");arr.add("ㄱ");arr.add("ㄱ");arr.add("ㅣ"); //닭기
        arr.add("ㅇ");arr.add("ㅗ");arr.add("ㅏ");arr.add("ㅇ");arr.add("ㅗ");arr.add("ㅐ");arr.add("ㅇ");arr.add("ㅜ");arr.add("ㅓ");arr.add("ㅇ");arr.add("ㅜ");arr.add("ㅔ");
        //arr.add("ㄷ");arr.add("ㅏ");arr.add("ㄹ");arr.add("ㄱ");arr.add("ㅈ");arr.add("ㅈ");arr.add("ㅣ");arr.add("ㄹ");arr.add("ㅜ");arr.add("ㅔ"); //닭끼
        System.out.println("초기 지화로 입력된 List : " + arr);

        //모음##########################################################################
        FindCollectionIndex(arr);
        if(collection_index.size() >= 2){
            for(int i = 1; i <= collection_index.size()- 1; i++){
                if(collection_index.get(i) - collection_index.get(i-1) == 1){
                    arr = FixCollection(arr,i);
                }
            }
            arr.removeAll(Collections.singleton(" "));
            System.out.println("모음 합성 후 Lisn : " + arr);
        }
        else{
            System.out.println(arr + "모음 합성 없음");
        }
        collection_index.clear();
        //##############################################################################


        //자음###########################################################################
        FindCollectionIndex(arr);
        if(collection_index.size() >= 2){
            for(int i = 1; i <= collection_index.size()- 1; i++){
                if(collection_index.get(i) - collection_index.get(i-1) - 1 >= 3){
                    arr = FixConsonant(arr,i);
                }
            }
            arr.removeAll(Collections.singleton(" "));
            System.out.println("자음 합성 후 지화 List : " + arr);
        }
        //##############################################################################


        //쌍자음##########################################################################
        String arr_value = "";
        for(int i = 0; i < arr.size(); i++){
            if(arr.get(i).equals(arr_value)) {
                char a = (char)(arr.get(i).codePointAt(0) + 1);
                arr.set(i,String.valueOf(a));
                arr.set(i-1," ");
            }
            arr_value = arr.get(i);
        }
        arr.removeAll(Collections.singleton(" "));
        //##############################################################################


        //Hangual Parser################################################################
        try {
            System.out.println("쌍자음 변환 후 List : " + arr + "\n한글 조합 후 출력문자 : " + HangulParser.assemble(arr));
        } catch (HangulParserException e) {
            e.printStackTrace();
        }
        //##############################################################################
    }



    public ArrayList<String> FixCollection(ArrayList<String> arr, int index){
        int fixIndex = arr.get(collection_index.get(index - 1)).codePointAt(0) + arr.get(collection_index.get(index)).codePointAt(0) - "ㅏ".codePointAt(0) * 2;
        arr.set(collection_index.get(index-1), fixedCollection.get(fixIndex));
        arr.set(collection_index.get(index)," ");
        return arr;
    }


    public ArrayList<String> FixConsonant(ArrayList<String> arr, int index){
        int fixIndex = arr.get(collection_index.get(index - 1) + 1).codePointAt(0) + arr.get(collection_index.get(index - 1) + 2).codePointAt(0) - "ㄱ".codePointAt(0) * 2;
        arr.set(collection_index.get(index - 1) + 1,fixedConsonant.get(fixIndex));
        arr.set(collection_index.get(index - 1) + 2," ");
        return arr;
    }

    public void FindCollectionIndex(ArrayList<String> arr){
        for(int i = 0; i < arr.size(); i++){
            if(arr.get(i).codePointAt(0) > 12622 && arr.get(i).codePointAt(0) < 12644){ //모음일경우
                collection_index.add(i);
            }
        }
    }
    */
}



//        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
//            @Override
//            public void onInit(int status) {
//                if(status != ERROR) {
//                    // 언어를 선택한다.
//                    tts.setLanguage(Locale.KOREAN);
//                }
//            }
//        });

//        button = (Button) findViewById(R.id.button);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                h.removeMessages(0);
//                h.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        System.out.println("start");
//                    }
//                },2000);
//            }
//        });
