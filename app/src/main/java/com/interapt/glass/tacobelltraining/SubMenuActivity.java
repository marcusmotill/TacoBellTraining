package com.interapt.glass.tacobelltraining;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.view.WindowUtils;

import javax.annotation.Nonnull;

import io.onthego.ari.KeyDecodingException;
import io.onthego.ari.android.ActiveAri;
import io.onthego.ari.android.Ari;
import io.onthego.ari.event.HandEvent;

public class SubMenuActivity extends Activity implements Ari.StartCallback, Ari.ErrorCallback,
        HandEvent.Listener {

    private static final int SPEECH_REQUEST = 0;
    private static int highlightCount = 0;
    private static SpeechRecognizer speechRecognizer;
    private static Intent speechRecognizerIntent;
    private static ImageView micImageView;
    private static ImageView loadingImageView;
    private static TextView subMenuTitleTextView;
    //private static TextView micPromptTextView;
    private static TextView readyMicTextView;
    private static TextView listeningMicTextView;
    private static TextView processingMicTextView;
    private static TextView menuItem1, menuItem2, menuItem3;
    private static TextView[] menuTextViews;
    private static RotateAnimation rotateAnimation;
    private static TextView partialSpeechResult;
    private static AudioManager audioManager;
    private static final String TAG = "SubMenuActivity";
    private ActiveAri mAri;
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
        //micPromptTextView = (TextView) findViewById(R.id.mic_prompt_textview2);
        readyMicTextView = (TextView) findViewById(R.id.ready_mic_prompt_textview2);
        listeningMicTextView = (TextView) findViewById(R.id.listening_mic_prompt_textview2);
        processingMicTextView = (TextView) findViewById(R.id.processing_mic_prompt_textview2);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        menuItem1 = (TextView) findViewById(R.id.sub_menu_item_1);
        menuItem2 = (TextView) findViewById(R.id.main_menu_item_2);
        menuItem3 = (TextView) findViewById(R.id.sub_menu_item_3);

        menuTextViews = new TextView[]{menuItem1, menuItem2, menuItem3};
        menuTextViews[highlightCount].setTextColor(Color.parseColor("#16b902"));

        try {
            mAri = ActiveAri.getInstance(getString(R.string.ari_license_key), this)
                    .addListeners(this)
                    .addErrorCallback(this);
        } catch (final KeyDecodingException e) {
            Log.e(TAG, "Failed to init Ari: ", e);
            finish();
        }

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
        if (mAri != null) {
            mAri.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        speechRecognizer.startListening(speechRecognizerIntent);
        mAri.start(this);
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
                Intent getIdIntent = new Intent(this, GetIdActivity.class);
                getIdIntent.putExtra("currentFoodItemNumber", currentFoodItemNumber);
                startActivity(getIdIntent);
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

    public void setMicPromptMessage(String message){
        //micPromptTextView.setText(message);
        switch(message){
            case "Speak Now":
                //micPromptTextView.setTextColor(Color.parseColor("#16b902"));
                readyMicTextView.setVisibility(View.VISIBLE);
                listeningMicTextView.setVisibility(View.INVISIBLE);
                processingMicTextView.setVisibility(View.INVISIBLE);
                break;
            case "Listening":
                //micPromptTextView.setTextColor(Color.parseColor("#cc3333"));
                readyMicTextView.setVisibility(View.INVISIBLE);
                listeningMicTextView.setVisibility(View.VISIBLE);
                processingMicTextView.setVisibility(View.INVISIBLE);
                break;
            case "Processing":
                //micPromptTextView.setTextColor(Color.parseColor("#5B5A5A"));
                readyMicTextView.setVisibility(View.INVISIBLE);
                listeningMicTextView.setVisibility(View.INVISIBLE);
                processingMicTextView.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
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

    @Override
    public void onHandEvent(HandEvent handEvent) {
        Log.i(TAG, "Ari " + handEvent.type);
        String eventType = handEvent.type.toString();

        if (eventType.equals("DOWN_SWIPE") || eventType.equals("UP_SWIPE")) {
            moveCursor(eventType);
        }else if(eventType.equals("CLOSED_HAND")){
            mAri.stop(); //to eliminate selecting twice
            selectMenuItem(highlightCount + 1);
        }else if(eventType.equals("OPEN_HAND")){
            mAri.stop();
            selectMenuItem(3);
        }

    }

    private void moveCursor(String eventType) {
        menuTextViews[highlightCount].setTextColor(getResources().getColor(R.color.white));
        if (eventType.equals("DOWN_SWIPE")) { // move down
            if (highlightCount != 2) {
                highlightCount++;
            } else {
                highlightCount = 0;
            }
        } else if (eventType.equals("UP_SWIPE")) { // move up
            if (highlightCount != 0) {
                highlightCount--;
            } else {
                highlightCount = 2;
            }
        }
        Log.i("Highlight Count", " " + highlightCount);
        menuTextViews[highlightCount].setTextColor(Color.parseColor("#16b902"));

    }

    @Override
    public void onAriStart() {
        // Enabling and disabling gestures is only available with Indie Developer and
        // Enterprise licenses.
         mAri.disable(HandEvent.Type.SWIPE_PROGRESS)
            .enable(HandEvent.Type.OPEN_HAND, HandEvent.Type.CLOSED_HAND,
                    HandEvent.Type.LEFT_SWIPE, HandEvent.Type.RIGHT_SWIPE,
                    HandEvent.Type.UP_SWIPE, HandEvent.Type.DOWN_SWIPE,
                    HandEvent.Type.THUMB_UP);
    }

    @Override
    public void onAriError(@Nonnull final Throwable throwable) {
        final String msg = "Ari error";
        Log.e(TAG, msg, throwable);

    }


}
