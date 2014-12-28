package com.interapt.mikenguyen.tacobelltraining;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.view.WindowUtils;

public class SubMenuActivity extends Activity {
    private static final int SPEECH_REQUEST = 0;
    private static SpeechRecognizer speechRecognizer;
    private static Intent speechRecognizerIntent;
    private static ImageView micImageView;
    private static ImageView loadingImageView;
    private static TextView subMenuTitleTextView;
    private static RotateAnimation rotateAnimation;
    private static TextView partialSpeechResult;
    private static AudioManager audioManager;
    private  static int currentFoodItemNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent mIntent = getIntent();
        currentFoodItemNumber = mIntent.getIntExtra("menuItemNumber", 0);
        Log.d("menu item number: ", String.valueOf(currentFoodItemNumber));
        setContentView(R.layout.activity_sub_menu);
        micImageView = (ImageView) findViewById(R.id.micImageView2);
        loadingImageView = (ImageView) findViewById(R.id.loadingImageView2);
        partialSpeechResult = (TextView) findViewById(R.id.speech_textview2);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initLoadingAnimation();
        initSpeechRecognition();
        initSubMenuTitleTextView();
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            getMenuInflater().inflate(R.menu.menu_sub_menu, menu);
            return true;
        }
        // Pass through to super to setup touch menu.
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public void onPause() {
        super.onPause();
        speechRecognizer.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        speechRecognizer.startListening(speechRecognizerIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sub_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            switch (item.getItemId()) {
                case R.id.one_menu_item:
                    Intent trainingIntent = new Intent(this, TrainingActivity.class);
                    if(currentFoodItemNumber != 0) {
                        trainingIntent.putExtra("currentFoodItemNumber", currentFoodItemNumber);
                        startActivity(trainingIntent);
                    }
                    break;
                case R.id.two_menu_item:
                    Intent testIntent = new Intent(this, TestActivity.class);
                    if(currentFoodItemNumber != 0) {
                        testIntent.putExtra("currentFoodItemNumber", currentFoodItemNumber);
                        startActivity(testIntent);
                    }
                    break;
                case R.id.three_menu_item:
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

    public void setMicImageView(Drawable micIcon){
        micImageView.setImageDrawable(micIcon);
    }

    // Toggle between showing/hiding the indeterminate slider.
    public void showLoadingAnimation(){
        loadingImageView.startAnimation(rotateAnimation);
    }

    public void hideLoadingAnimation(){
        loadingImageView.setAnimation(null);
    }

    public void setPartialSpeechResult(String partialResult){
        partialSpeechResult.setText(partialResult);
    }

    public void selectMenuItem(int menuItemNumber){
        switch (menuItemNumber){
            case 1:
                playSuccessSound();
                Intent trainingIntent = new Intent(this, TrainingActivity.class);
                trainingIntent.putExtra("currentFoodItemNumber", currentFoodItemNumber);
                startActivity(trainingIntent);
                break;
            case 2:
                playSuccessSound();
                Intent testIntent = new Intent(this, TestActivity.class);
                testIntent.putExtra("currentFoodItemNumber", currentFoodItemNumber);
                startActivity(testIntent);
                break;
            case 3:
                playSuccessSound();
                finish();
                break;
            default:
                playDisallowedSound();
                break;
        }
    }

    public void playSuccessSound(){
        audioManager.playSoundEffect(Sounds.SUCCESS);
    }

    public void playDisallowedSound(){
        audioManager.playSoundEffect(Sounds.DISALLOWED);
    }

    //Speech recognition initialization
    private void initSpeechRecognition(){
        Context context = this.getApplicationContext();
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new SpeechListener(this, context, speechRecognizer, speechRecognizerIntent));
    }

    private void initLoadingAnimation(){
        rotateAnimation = new RotateAnimation(0f, 350f, 48f, 48f);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setDuration(700);
    }

    private void initSubMenuTitleTextView(){
        subMenuTitleTextView = (TextView) findViewById(R.id.sub_menu_title);
        String title = "";
        switch (currentFoodItemNumber) {
            case 1:
                title = "Triple Steak Stack";
                break;
            case 2:
                title = "Chicken Triple Steak Stack";
                break;
            case 3:
                title = "Cinnabon Coffee";
                break;
            case 4:
                title = "Iced Coffee";
                break;
            case 5:
                title = "Cheesy Burrito";
                break;
            default:
                break;
        }
        if(title != "") {
            subMenuTitleTextView.setText(title);
        }
    }
}
