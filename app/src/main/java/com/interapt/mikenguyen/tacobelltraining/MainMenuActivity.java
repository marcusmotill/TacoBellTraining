package com.interapt.mikenguyen.tacobelltraining;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.view.WindowUtils;

import java.util.List;


public class MainMenuActivity extends Activity {

    private static final int SPEECH_REQUEST = 0;
    private static SpeechRecognizer speechRecognizer;
    private static Intent speechRecognizerIntent;
    private static ImageView micImageView;
    private static ImageView loadingImageView;
    private static RotateAnimation rotateAnimation;
    private static TextView partialSpeechResult;
    private static AudioManager audioManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        micImageView = (ImageView) findViewById(R.id.micImageView);
        loadingImageView = (ImageView) findViewById(R.id.loadingImageView);
        partialSpeechResult = (TextView) findViewById(R.id.speech_textview);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initLoadingAnimation();
        initSpeechRecognition();
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
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            getMenuInflater().inflate(R.menu.menu_main_menu, menu);
            return true;
        }
        // Pass through to super to setup touch menu.
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            switch (item.getItemId()) {
                case R.id.one_menu_item:
                    startSubMenuActivity(1);
                    break;
                case R.id.two_menu_item:
                    startSubMenuActivity(2);
                    break;
                case R.id.three_menu_item:
                    startSubMenuActivity(3);
                    break;
                case R.id.four_menu_item:
                    startSubMenuActivity(4);
                    break;
                case R.id.five_menu_item:
                    startSubMenuActivity(5);
                    break;
                default:
                    return true;
            }
            return true;
        }
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

    public void startSubMenuActivity(int menuItemNumber){
        audioManager.playSoundEffect(Sounds.SUCCESS);
        Intent myIntent = new Intent(this, SubMenuActivity.class);
        myIntent.putExtra("menuItemNumber", menuItemNumber);
        startActivity(myIntent);
    }

    public void playDisallowedSound(){
        audioManager.playSoundEffect(Sounds.DISALLOWED);
    }

    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        startActivityForResult(intent, SPEECH_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            // Do something with spokenText.
        }
        super.onActivityResult(requestCode, resultCode, data);
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
}
