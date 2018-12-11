package com.sentongoapps.sogetiaranimal;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class PreferenceMenu extends Dialog{

    public Activity activity;
    public Dialog dialog;
    private RadioGroup animationRadioGroup;
    private RadioGroup directionRadioGroup;
    private Button saveButton;
    private AnimalNode.MotionAnimation motionAnimation;
    private AnimalNode.MotionDirection motionDirection;
    private PreferenceService preferenceService;

    public PreferenceMenu(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.preference_menu);
        preferenceService = new PreferenceService(activity);
        animationRadioGroup = findViewById(R.id.animation_group);
        animationRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int resourceId = i;
                RadioButton checkButton = findViewById(resourceId);
                switch (i){
                    case R.id.radio_rotation:
                        if(checkButton.isChecked()){
                            motionAnimation = AnimalNode.MotionAnimation.ROTATION;
                        }
                        break;
                    case R.id.radio_linear:
                        if(checkButton.isChecked()){
                            motionAnimation = AnimalNode.MotionAnimation.LINEAR;
                        }
                        break;
                    case R.id.radio_kill:
                        if(checkButton.isChecked()){
                            motionAnimation = AnimalNode.MotionAnimation.KILL;
                        }
                        break;
                }
            }
        });


        directionRadioGroup = findViewById(R.id.direction_group);
        directionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int resourceId = i;
                RadioButton checkButton = findViewById(resourceId);
                switch (i){
                    case R.id.radio_left:
                        if(checkButton.isChecked()){
                            motionDirection = AnimalNode.MotionDirection.LEFT;
                        }
                        break;
                    case R.id.radio_right:
                        if(checkButton.isChecked()){
                            motionDirection = AnimalNode.MotionDirection.RIGHT;
                        }
                        break;
                    case R.id.radio_forward:
                        if(checkButton.isChecked()){
                            motionDirection = AnimalNode.MotionDirection.FORWARD;
                        }
                        break;
                    case R.id.radio_backward:
                        if(checkButton.isChecked()){
                            motionDirection = AnimalNode.MotionDirection.BACKWARD;
                        }
                        break;
                }
            }
        });
        saveButton = findViewById(R.id.button_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (motionAnimation != null) {
                    preferenceService.setMotionAnimation(motionAnimation);
                }
                if (motionDirection != null) {
                    preferenceService.setMotionDirection(motionDirection);
                }
                dismiss();
            }
        });
    }
}