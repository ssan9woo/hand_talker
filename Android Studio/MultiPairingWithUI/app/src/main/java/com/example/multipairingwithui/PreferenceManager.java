package com.example.multipairingwithui;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

public class PreferenceManager {
    public static final String PREFERENCES_NAME = "SHARE_PREF";
    public static final String LEN_PREFIX = "Count_";
    public static final String VAL_PREFIX = "IntValue_";
    public static final String left = "LEFT";
    public static final String right = "RIGHT";
    public static final String wordlist = "WORDLIST";
    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

    }

    public static int[] getUserdata(String name,Context context){
        SharedPreferences sharePref = getPreferences(context);
        int[] ret;
        int count = sharePref.getInt(LEN_PREFIX + name, 0);
        ret = new int[count];
        for (int i = 0; i < count; i++){
            ret[i] = sharePref.getInt(VAL_PREFIX+ name + i, i);
        }
        return ret;
    }
    public static void save_flex_value(int id, int rock_or_paper,int[] flex_values, Context context){
        SharedPreferences sharePref = getPreferences(context);
        SharedPreferences.Editor editor = sharePref.edit();
        String name = bluetoothService.str_hand[id]+bluetoothService.str_rock_or_paper[rock_or_paper];
        editor.putInt(LEN_PREFIX + name, flex_values.length);
        int count = 0;
        for (int i: flex_values){
            editor.putInt(VAL_PREFIX + name + count++, i);
        }
        editor.apply();
    }
    public static void save_syllable_value(Hand hand,String name, Context context){
        SharedPreferences sharePref = getPreferences(context);
        @SuppressLint("CommitPrefEdits")
        Editor editor = new Editor(sharePref.edit());
        editor.putInt(LEN_PREFIX+name,bluetoothService.right_hand.getDatalength());
        int count = 0;
        if(hand.hand=="RIGHT") {
            for (double i : hand.getGyro()) {
                editor.putDouble(editor, VAL_PREFIX + name + count++, i);
            }
            for (int i : hand.getFlex()) {
                editor.putInt(VAL_PREFIX + name + count++, i);
            }
            for (boolean i : hand.getTouch()) {
                editor.putBoolean(VAL_PREFIX + name + count++, i);
            }
        }
        editor.apply();
    }
    public static Syllable get_syllable_value(String name,String chr, Context context){
        SharedPreferences sharePref = getPreferences(context);
        String key = name+chr;
        Syllable syllable = new Syllable(chr);
        double[] gyro = new double[3];
        int[] flex = new int[6];
        boolean[] touch = new boolean[2];
        int count = sharePref.getInt(LEN_PREFIX + key, 0);
        for (int i = 0; i < count; i++) {
            if (i > 8) {
                touch[i-9] = sharePref.getBoolean(VAL_PREFIX+ key+i, false);
            } else if (i > 2) {
                flex[i-3] = sharePref.getInt(VAL_PREFIX+key+i, 0);
            } else {
                gyro[i] = getDouble(sharePref, VAL_PREFIX + key+i, 0);
            }
            syllable.setTouch(touch);
            syllable.setGyro(gyro);
            syllable.setFlex(flex);
        }
        return syllable;
    }

    public static void remove_syllable_value(String name, Context context){
        SharedPreferences sharePref = getPreferences(context);
        @SuppressLint("CommitPrefEdits")
        Editor editor = new Editor(sharePref.edit());
        int count = sharePref.getInt(LEN_PREFIX + name, 0);
        editor.remove(LEN_PREFIX+name);
        for(int i=0; i<count;i++){
            editor.remove(VAL_PREFIX+name+i);
        }
        editor.apply();
    }

    public static void save_word_value(Hand hand,String word, Context context){
        int count = 0;

        SharedPreferences sharePref = getPreferences(context);
        @SuppressLint("CommitPrefEdits")
        Editor editor = new Editor(sharePref.edit());
        editor.putInt(LEN_PREFIX + hand.hand + word,hand.getDatalength());

        String key= VAL_PREFIX + hand.hand + word;


        if(hand.hand.equals("RIGHT")) {


            for (double i : hand.getGyro()) {
                editor.putDouble(editor, key + count++, i);
            }
            for (int i : hand.getFlex()) {
                editor.putInt(key + count++, i);
            }
            for (boolean i : hand.getTouch()) {
                editor.putBoolean(key + count++, i);
            }
        }
        else{

            String list = sharePref.getString(wordlist,"");
            editor.remove(wordlist);
            assert list != null;
            if(!list.equals("")) {
                editor.putString(wordlist, list + "," + word);
            }
            else{
                editor.putString(wordlist,word);
            }

            for (double i : hand.getGyro()) {
                editor.putDouble(editor, key + count++, i);
            }
            for (int i : hand.getFlex()) {
                editor.putInt(key + count++, i);
            }
        }
        editor.apply();
    }
    public static Word get_word_value(String str, Context context){
        SharedPreferences sharePref = getPreferences(context);
        Word word = new Word(str);

        double[] left_gyro = new double[3];
        int[] left_flex = new int[5];
        double[] right_gyro = new double[3];
        int[] right_flex = new int[6];
        boolean[] touch = new boolean[2];
        int left_count = sharePref.getInt(LEN_PREFIX + left + str, 0);
        int right_count = sharePref.getInt(LEN_PREFIX + right + str, 0);

        for (int i = 0; i < left_count; i++) {
            if (i > 2) {
                left_flex[i-3] = sharePref.getInt(VAL_PREFIX+ left + str + i, 0);
            } else {
                left_gyro[i] = getDouble(sharePref, VAL_PREFIX + left + str + i, 0);
            }
        }

        for (int i = 0; i < right_count; i++) {
            if (i > 8) {
                touch[i-9] = sharePref.getBoolean(VAL_PREFIX+ right + str + i, false);
            } else if (i > 2) {
                right_flex[i-3] = sharePref.getInt(VAL_PREFIX+ right + str + i, 0);
            } else {
                right_gyro[i] = getDouble(sharePref, VAL_PREFIX + right + str + i, 0);
            }
        }
        word.set_touch(touch);
        word.set_gyro(left_gyro,right_gyro);
        word.set_flex(left_flex ,right_flex);
        return word;
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void remove_word_value(String word, Context context){
        SharedPreferences sharePref = getPreferences(context);
        @SuppressLint("CommitPrefEdits")
        Editor editor = new Editor(sharePref.edit());

        int left_count = sharePref.getInt(LEN_PREFIX + left + word, 0);
        int right_count = sharePref.getInt(LEN_PREFIX + right + word, 0);
        StringBuilder words = new StringBuilder(Objects.requireNonNull(sharePref.getString(wordlist, "")));
        if(!isEmptyWordList(context)) {
            try {
                String[] array = words.toString().split(",");
                List<String> list = new ArrayList<>(Arrays.asList(array));
                list.remove((Object)word );
                words = new StringBuilder();
                for (int i = 0; i < list.size(); i++) {
                    words.append(list.get(i));
                    if (i < list.size() - 1) {
                        words.append(",");
                    }
                }
                editor.remove(wordlist);
                editor.putString(wordlist, words.toString());
            } catch (PatternSyntaxException e) {
                editor.remove(wordlist);
            }
            editor.remove(LEN_PREFIX + left + word);
            editor.remove(LEN_PREFIX + right + word);
            for (int i = 0; i < left_count; i++) {
                editor.remove(VAL_PREFIX + left + word + i);
            }
            for (int i = 0; i < right_count; i++) {
                editor.remove(VAL_PREFIX + right + word + i);
            }
        }
        editor.apply();
    }
    public static boolean isEmptyWordList(Context context) {
        SharedPreferences sharePref = getPreferences(context);
        String str = sharePref.getString(wordlist, "");
        assert str != null;
        return str.equals("");
    }
    public static String[] getWordList(Context context){
        SharedPreferences sharePref = getPreferences(context);
        String str = sharePref.getString(wordlist, "");
        try {
            assert str != null;
            return str.split(",");
        }catch(PatternSyntaxException e){
            return new String[0];
        }
    }
    public static boolean IskeyinPref(String name, Context context){
        SharedPreferences sharePref = getPreferences(context);
        int count = sharePref.getInt(LEN_PREFIX + name, 0);
        return count > 0;
    }


    public static void clear(Context context) {
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.apply();
    }
    public static class Editor implements SharedPreferences.Editor {
        private SharedPreferences.Editor _editor;

        public Editor(SharedPreferences.Editor editor) {
            _editor = editor;
        }

        private Editor ReturnEditor(SharedPreferences.Editor editor) {
            if(editor instanceof Editor)
                return (Editor)editor;
            return new Editor(editor);
        }

        @Override
        public Editor putString(String key, String value) {
            return ReturnEditor(_editor.putString(key, value));
        }

        @Override
        public Editor putStringSet(String key, Set<String> values) {
            return ReturnEditor(_editor.putStringSet(key, values));
        }

        @Override
        public Editor putInt(String key, int value) {
            return ReturnEditor(_editor.putInt(key, value));
        }

        @Override
        public Editor putLong(String key, long value) {
            return ReturnEditor(_editor.putLong(key, value));
        }

        @Override
        public Editor putFloat(String key, float value) {
            return ReturnEditor(_editor.putFloat(key, value));
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            return ReturnEditor(_editor.putBoolean(key, value));
        }

        @Override
        public Editor remove(String key) {
            return ReturnEditor(_editor.remove(key));
        }

        @Override
        public Editor clear() {
            return ReturnEditor(_editor.clear());
        }

        @Override
        public boolean commit() {
            return _editor.commit();
        }

        @Override
        public void apply() {
            _editor.apply();
        }

        Editor putDouble(final Editor edit, final String key, final double value) {
            return edit.putLong(key, Double.doubleToRawLongBits(value));
        }
    }
    static double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }
}