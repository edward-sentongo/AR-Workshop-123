package com.sentongoapps.sogetiaranimal;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class PreferenceService {

    private String PREF_NAME = "AnimalAppPreferences";
    static String KEY_MOTION_DIRECTION = "KeyMotionDirection";
    static String KEY_MOTION_ANIMATION = "KeyMotionAnimation";
    SharedPreferences sharedPreferences;

    public PreferenceService(Context context){
        sharedPreferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
    }

    public void setMotionDirection(AnimalNode.MotionDirection motionDirection){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_MOTION_DIRECTION, motionDirection.ordinal());
        editor.apply();
    }

    public void setMotionAnimation(AnimalNode.MotionAnimation motionAnimation){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_MOTION_ANIMATION, motionAnimation.ordinal());
        editor.apply();
    }

    public AnimalNode.MotionDirection getMotionDirection(){
        return AnimalNode.MotionDirection.fromInteger(sharedPreferences.getInt(KEY_MOTION_DIRECTION, 0));
    }

    public AnimalNode.MotionAnimation getMotionAnimation(){
        return AnimalNode.MotionAnimation.fromInteger(sharedPreferences.getInt(KEY_MOTION_ANIMATION, 0));
    }
}


