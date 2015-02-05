package com.interapt.glass.tacobelltraining;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.view.WindowUtils;

import javax.annotation.Nonnull;

import io.onthego.ari.KeyDecodingException;
import io.onthego.ari.android.ActiveAri;
import io.onthego.ari.android.Ari;
import io.onthego.ari.event.HandEvent;


public class TrainingActivity extends Activity implements Ari.StartCallback, Ari.ErrorCallback,
        HandEvent.Listener {

    private int currentFoodItemNumber = 0;
    private RelativeLayout trainingLayout;
    private FoodItem foodItem;
    private TextView timerTextView;
    private CountDownTimer countDownTimer;
    private ImageView playPauseImageView;
    private static AudioManager audioManager;
    private static final String LEFT_SWIPE = "LEFT_SWIPE";
    private static final String RIGHT_SWIPE = "RIGHT_SWIPE";
    private static final String UP_SWIPE = "UP_SWIPE";
    private static final String DOWN_SWIPE = "DOWN_SWIPE";
    //private TextSpeaker textSpeaker;

    /**
     * The amount of time to show each step
     */
    private static final long STEP_DISPLAY_TIME = 9000;
    /**
     * Handler used to post a delayed the display of each step
     */
    private final Handler mHandler = new Handler();
    //Boolean to play/pause the training
    private boolean play = true;
    //Keep track of time left in the count down timer
    private long currentTime = 0;

    private static final String TAG = "CameraViewActivity";


    private ActiveAri mAri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Requests a voice menu on this activity.
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        //init
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        textSpeaker = new TextSpeaker(getApplicationContext(), "TrainingActivity");
//        textSpeaker.speakMessage(11);
        timerTextView = (TextView) findViewById(R.id.timer_texview);
        playPauseImageView = (ImageView) findViewById(R.id.play_pause_imageview);
        countDownTimer = new CountDownTimer(STEP_DISPLAY_TIME + 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                if (play) {
                    currentTime = millisUntilFinished / 1000;
                    timerTextView.setText(currentTime + "S");
                }
            }

            public void onFinish() {

            }
        };
        Intent mIntent = getIntent();
        currentFoodItemNumber = mIntent.getIntExtra("currentFoodItemNumber", 0);
        trainingLayout = (RelativeLayout) findViewById(R.id.training_layout);
        if (currentFoodItemNumber != 0) {
            initializeFoodItem();
            displayStepImage();
        }

//        try {
//            mAri = ActiveAri.getInstance(getString(R.string.ari_license_key), this)
//                    .addListeners(this)
//                    .addErrorCallback(this);
//        } catch (final KeyDecodingException e) {
//            Log.e(TAG, "Failed to init Ari: ", e);
//            //finish();
//        }
        //textSpeaker.speakMessage(1000);
        startTraining();
    }

    @Override
    protected void onPause() {
        super.onPause();
        play = false;
//        if(textSpeaker != null && textSpeaker.isSpeaking()){
//            textSpeaker.stopSpeaking();
//            textSpeaker.destroy();
//        }
        //stopAri();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if(textSpeaker == null){
//            textSpeaker = new TextSpeaker(getApplicationContext(), "TrainingActivity");
//        }
        //mAri.start(this);
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
                    startTraining();
                    break;
                case R.id.pause_video_menu_item:
                    pauseTraining();
                    break;
                case R.id.go_forward_menu_item:
                    manualNextStep();
                    break;
                case R.id.go_back_menu_item:
                    pauseTraining();
                    previousStep();
                    break;
                case R.id.retry_menu_item:
                    resetTraining();
                    break;
                case R.id.dismiss_menu_item:
                    finish();
                    break;
                default:
                    return true;
            }
            return true;
        }
        // Good practice to pass through to super if not handled
        return super.onMenuItemSelected(featureId, item);
    }

    private void initializeFoodItem() {
        foodItem = new FoodItem(currentFoodItemNumber);
    }

    private void displayStepImage() {
//        if(foodItem.isFinishTraining()){
//            textSpeaker.speakMessage(1000);
//        } else{
//            playCurrentAudioPrompt();
//        }
        int imageResource = getResources().getIdentifier(foodItem.getCurrentStepImage(), "drawable", getPackageName());
        Drawable stepImage = getResources().getDrawable(imageResource);
        trainingLayout.setBackground(stepImage);
    }

    private void nextStep() {
        playNavigationSound();
        foodItem.nextStep();
        displayStepImage();
        if (!foodItem.isFinishTraining()) {
            mHandler.postDelayed(stepDelayRunnable, STEP_DISPLAY_TIME);
            countDownTimer.start();
        }
    }

    private void manualNextStep() {
//        if(textSpeaker.isSpeaking()){
//            textSpeaker.stopSpeaking();
//        }
        if (!foodItem.isFinishTraining()) {
            if (foodItem.isLastStep()) {
                playPauseImageView.setVisibility(View.INVISIBLE);
                timerTextView.setText("");
            } else {
                playPauseImageView.setVisibility(View.VISIBLE);
                pauseTraining();
            }
            foodItem.nextStep();
            displayStepImage();
        } else {
            startTestActivity();
            finish();
        }
    }

    private void previousStep() {
//        if(textSpeaker.isSpeaking()){
//            textSpeaker.stopSpeaking();
//        }
        if (!foodItem.previousStep()) {
            if (foodItem.isFinishTraining()) {
                foodItem.setFinishTraining(false);
                playPauseImageView.setVisibility(View.VISIBLE);
            }
            displayStepImage();
        } else {
            finish();
        }
    }

    private void resetTraining() {
        if (foodItem.isFinishTraining()) {
            foodItem.setFinishTraining(false);
            playPauseImageView.setVisibility(View.VISIBLE);
        }
        play = true;
        foodItem.setCurrentStep(1);
        displayStepImage();
        startTraining();
    }

    private void pauseTraining() {
        play = false;
        setPlayPauseIcon(false);
        timerTextView.setText("");
    }

    private void startTraining() {
        if (!foodItem.isFinishTraining()) {
            play = true;
            setPlayPauseIcon(true);
            mHandler.postDelayed(stepDelayRunnable, STEP_DISPLAY_TIME);
            countDownTimer.start();
        }
    }

    private void setPlayPauseIcon(boolean playPause) {
        //if playPause is true, set play icon & vice versa for pause icon
        int imageResource = 0;
        if (playPause) {
            imageResource = getResources().getIdentifier("ic_music_play_50", "drawable", getPackageName());
        } else {
            imageResource = getResources().getIdentifier("ic_music_pause_50", "drawable", getPackageName());
        }
        if (imageResource != 0) {
            Drawable playIcon = getResources().getDrawable(imageResource);
            playPauseImageView.setImageDrawable(playIcon);
        }
    }

    private void playNavigationSound(){
        audioManager.playSoundEffect(Sounds.SELECTED);
    }

    private void playSuccessSound(){
        audioManager.playSoundEffect(Sounds.SUCCESS);
    }

//    private void playCurrentAudioPrompt(){
//        textSpeaker.speakMessage((currentFoodItemNumber) * 10 + foodItem.getCurrentStep());
//        Log.i("Text Speaker", String.valueOf((currentFoodItemNumber) * 10 + foodItem.getCurrentStep()));
//    }

    private void starGetIdActivity() {
        Intent getIdIntent = new Intent(this, GetIdActivity.class);
        getIdIntent.putExtra("currentFoodItemNumber", currentFoodItemNumber);
        startActivity(getIdIntent);
        finish();
        Log.d("Training: ", "Starting get id activity");
    }

    private void startTestActivity(){
        Intent testIntent = new Intent(this, TestActivity.class);
        testIntent.putExtra("employeeId", "SAMPLE");
        testIntent.putExtra("currentFoodItemNumber", currentFoodItemNumber);
        startActivity(testIntent);
    }

    private void stopAri(){
        if(mAri!=null){
            mAri.stop();
        }
    }

    private Runnable stepDelayRunnable = new Runnable() {
        public void run() {
            if (play && currentTime <= 1) {
                if (!foodItem.isLastStep()) {
                    nextStep();
                } else {
                    nextStep();
                    timerTextView.setText("");
                    playPauseImageView.setVisibility(View.INVISIBLE);
                }
            }
        }
    };

    @Override
    public void onHandEvent(HandEvent handEvent) {
        Log.i(TAG, "Ari " + handEvent.type);
        String eventType = handEvent.type.toString();
        switch(eventType){
            case DOWN_SWIPE:
                stopAri();
                playSuccessSound();
                finish();
                break;
            case UP_SWIPE:
                playSuccessSound();
                if (play) {
                    pauseTraining();
                } else {
                    startTraining();
                }
                break;
            case LEFT_SWIPE:
                playSuccessSound();
                manualNextStep();
                break;
            case RIGHT_SWIPE:
                playSuccessSound();
                previousStep();
                break;
            default:
                break;
        }
    }

    @Override
    public void onAriStart() {
        // Enabling and disabling gestures is only available with Indie Developer and
        // Enterprise licenses.
         mAri.disable(HandEvent.Type.SWIPE_PROGRESS, HandEvent.Type.CLOSED_HAND,
                 HandEvent.Type.OPEN_HAND, HandEvent.Type.V_SIGN)
             .enable(HandEvent.Type.DOWN_SWIPE, HandEvent.Type.UP_SWIPE,
                     HandEvent.Type.LEFT_SWIPE, HandEvent.Type.RIGHT_SWIPE);
    }

    @Override
    public void onAriError(@Nonnull final Throwable throwable) {
        final String msg = "Ari error";
        Log.e(TAG, msg, throwable);
    }

}
