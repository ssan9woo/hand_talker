package com.example.multipairingwithui;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Arrays;
import java.util.Set;

public class PreferenceManager {
    public static final String PREFERENCES_NAME = "SHARE_PREF";
    public static final String LEN_PREFIX = "Count_";
    public static final String VAL_PREFIX = "IntValue_";
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
    public static void save_gesture_value(Hand hand,String name, Context context){
        SharedPreferences sharePref = getPreferences(context);
        @SuppressLint("CommitPrefEdits")
        Editor editor = new Editor(sharePref.edit());
        editor.putInt(LEN_PREFIX+name,bluetoothService.right_hand.getDatalength());
        int count = 0;
        if(hand.hand=="RIGHT"){
            for(double i:hand.getGyro()){
                editor.putDouble( editor,VAL_PREFIX+name+count++,i);
            }
            for(int i: hand.getFlex()){
                editor.putInt(VAL_PREFIX+name+count++,i);
            }
            for(boolean i:hand.getTouch()){
                editor.putBoolean(VAL_PREFIX+name+count++,i);
            }
        }
        else {
            for(double i:hand.getGyro()){
                editor.putDouble( editor,VAL_PREFIX+name+count++,i);
            }
            for(int i: hand.getFlex()){
                editor.putInt(VAL_PREFIX+name+count++,i);
            }
        }
        editor.apply();
    }
    public static Syllable get_gesture_value(String name,String chr, Context context){
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
    public static boolean IskeyinPref(String name, Context context){
        SharedPreferences sharePref = getPreferences(context);
        int count = sharePref.getInt(LEN_PREFIX + name, 0);
        return count > 0;
    }

    public static void remove_gesture_value(String name, Context context){
        SharedPreferences sharePref = getPreferences(context);
        @SuppressLint("CommitPrefEdits")
        Editor editor = new Editor(sharePref.edit());
        int count = sharePref.getInt(LEN_PREFIX + name, 0);
        editor.remove(LEN_PREFIX+name);
        Log.d("remove",name);
        for(int i=0; i<count;i++){
            editor.remove(VAL_PREFIX+name+i);
        }
        editor.apply();
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

        //region Overrides

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