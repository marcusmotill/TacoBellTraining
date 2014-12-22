package com.interapt.mikenguyen.tacobelltraining;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.glass.view.WindowUtils;


public class TrainingActivity extends Activity {

    private int currentFoodItemNumber = 0;
    private RelativeLayout trainingLayout;
    private FoodItem foodItem;
    private TextView timerTextView;
    private CountDownTimer countDownTimer;
    /** The amount of time to show each step */
    private static final long STEP_DISPLAY_TIME = 7000;
    /** Handler used to post a delayed the display of each step */
    private final Handler mHandler = new Handler();
    //Boolean to play/pause the training
    private boolean play = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Requests a voice menu on this activity.
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        //init
        timerTextView = (TextView) findViewById(R.id.timer_texview);
        countDownTimer =  new CountDownTimer(8000, 1000) {

            public void onTick(long millisUntilFinished) {
                if(play){
                    timerTextView.setText(millisUntilFinished / 1000 + "S");
                }
            }

            public void onFinish() {

            }
        };
        Intent mIntent = getIntent();
        currentFoodItemNumber = mIntent.getIntExtra("currentFoodItemNumber", 0);
        trainingLayout = (RelativeLayout) findViewById(R.id.training_layout);
        if(currentFoodItemNumber != 0){
            initializeFoodItem();
            displayStepImage();
        }
        startPlayingStep();
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            getMenuInflater().inflate(R.menu.menu_training, menu);
            return true;
        }
        // Pass through to super to setup touch menu.
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_training, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            play = false;
            switch (item.getItemId()) {
                case R.id.play_video_menu_item:
                    playTraining();
                    break;
                case R.id.pause_video_menu_item:
                    pauseTraining();
                    break;
                case R.id.go_forward_menu_item:
                    nextStep();
                    break;
                case R.id.go_back_menu_item:
                    previousStep();
                    break;
                case R.id.retry_menu_item:
                    resetTraining();
                    break;
                default:
                    return true;
            }
            return true;
        }
        // Good practice to pass through to super if not handled
        return super.onMenuItemSelected(featureId, item);
    }

    private void initializeFoodItem(){
        foodItem = new FoodItem(currentFoodItemNumber);
    }

    private void displayStepImage() {
        int imageResource = getResources().getIdentifier(foodItem.getCurrentStepImage(), "drawable", getPackageName());
        Drawable stepImage = getResources().getDrawable(imageResource);
        trainingLayout.setBackground(stepImage);
    }

    private void nextStep(){
        foodItem.nextStep();
        displayStepImage();
    }

    private void previousStep(){
        foodItem.previousStep();
        displayStepImage();
        play = false;
    }

    private void resetTraining(){
        foodItem.setCurrentStep(1);
        displayStepImage();
    }

    private void playTraining() {
        play = true;
        startPlayingStep();
    }

    private void pauseTraining(){
        play = false;
        timerTextView.setText("");
    }

    private void startPlayingStep(){
        mHandler.postDelayed(stepDelayRunnable, STEP_DISPLAY_TIME);
        countDownTimer.start();
    }

    private void createCountDownTimer(long TIME) {

    }

    private Runnable stepDelayRunnable = new Runnable() {
        public void run() {
            if(play){
                if(foodItem.getCurrentStep() != foodItem.getNumberOfSteps()){
                    nextStep();
                    mHandler.postDelayed(stepDelayRunnable, STEP_DISPLAY_TIME);
                    countDownTimer.start();
                }
            }
        }
    };
}
