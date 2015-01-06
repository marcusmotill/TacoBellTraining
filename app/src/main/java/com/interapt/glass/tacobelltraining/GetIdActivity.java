package com.interapt.glass.tacobelltraining;

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
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;

import io.onthego.ari.KeyDecodingException;
import io.onthego.ari.android.ActiveAri;
import io.onthego.ari.android.Ari;
import io.onthego.ari.event.HandEvent;


public class GetIdActivity extends Activity implements Ari.StartCallback, Ari.ErrorCallback,
        HandEvent.Listener{

    private final int ID_LENGTH = 6;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private ImageView micImageView;
    private ImageView loadingImageView;
    private RotateAnimation rotateAnimation;
    private TextView employeeIdTextView;
    private TextView promptTextView;
    private TextView yesTextView;
    private TextView noTextView;
    private AudioManager audioManager;
    private String employeeID = "";
    private int currentFoodItemNumber = 0;
    private boolean gotUserId = false;
    private ActiveAri mAri;
    private String TAG = "GetIDActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent mIntent = getIntent();
        currentFoodItemNumber = mIntent.getIntExtra("currentFoodItemNumber", 0);
        Log.d("menu item number: ", String.valueOf(currentFoodItemNumber));
        setContentView(R.layout.activity_get_id);
        micImageView = (ImageView) findViewById(R.id.micImageView3);
        loadingImageView = (ImageView) findViewById(R.id.loadingImageView3);
        employeeIdTextView = (TextView) findViewById(R.id.employee_id_textview);
        promptTextView = (TextView) findViewById(R.id.prompt_textview);
        yesTextView = (TextView) findViewById(R.id.yes_textview);
        noTextView = (TextView) findViewById(R.id.no_texview);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_get_id, menu);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAri != null) {
            mAri.stop();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    public void selectMenuItem(int userSelection){
        //1 is "yes", 2 is "no"
        switch (userSelection){
            case 1:
                if(employeeID.length() == ID_LENGTH){
                    playSuccessSound();
                    Intent testIntent = new Intent(this, TestActivity.class);
                    testIntent.putExtra("employeeId", employeeID);
                    testIntent.putExtra("currentFoodItemNumber", currentFoodItemNumber);
                    startActivity(testIntent);
                    finish();
                }
                break;
            case 2:
                playSuccessSound();
                employeeID = "";
                gotUserId = false;
                promptTextView.setText("Say your Taco Bell ID");
                showHideYesNoPrompts(false);
                break;
            default:
                break;
        }
    }

    public void playSuccessSound(){
        audioManager.playSoundEffect(Sounds.SUCCESS);
    }

    public void playDisallowedSound(){
        audioManager.playSoundEffect(Sounds.DISALLOWED);
    }

    public boolean isGotUserId(){
        return gotUserId;
    }

    public void parseUserId(String speechResult){
        String noSpaceString = speechResult.replaceAll("\\s","");
        String upperCaseString = noSpaceString.toUpperCase();
        if(upperCaseString.length() == ID_LENGTH){
            playSuccessSound();
            employeeIdTextView.setText(upperCaseString);
            promptTextView.setText("Is this correct?");
            showHideYesNoPrompts(true);
            employeeID = upperCaseString;
            gotUserId = true;
        } else {
            playDisallowedSound();
            //employeeIdTextView.setText(noSpaceString.toUpperCase());
            promptTextView.setText("The ID format is incorrect, try again");
        }
    }

    private void showHideYesNoPrompts(boolean show){
        if(show){
            yesTextView.setVisibility(View.VISIBLE);
            noTextView.setVisibility(View.VISIBLE);
        } else {
            yesTextView.setVisibility(View.INVISIBLE);
            noTextView.setVisibility(View.INVISIBLE);
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

    @Override
    public void onAriError(Throwable throwable) {
        final String msg = "Ari error";
        Log.e(TAG, msg, throwable);
    }

    @Override
    public void onHandEvent(HandEvent handEvent) {
        Log.i(TAG, "Ari " + handEvent.type);
        String eventType = handEvent.type.toString();

        if (eventType.equals("LEFT_SWIPE")) {

        } else if(eventType.equals("CLOSED_HAND")){
            if(yesTextView.getVisibility() == View.VISIBLE){
                selectMenuItem(1);
                mAri.stop();
            }

        } else if(eventType.equals("OPEN_HAND")){
            if(yesTextView.getVisibility() == View.VISIBLE){
                selectMenuItem(2);
            } else if(yesTextView.getVisibility() == View.INVISIBLE){
                finish();
                mAri.stop();
            }
        }
    }

    @Override
    public void onAriStart() {
        mAri.disable(HandEvent.Type.SWIPE_PROGRESS)
                .enable(HandEvent.Type.OPEN_HAND, HandEvent.Type.CLOSED_HAND,
                        HandEvent.Type.LEFT_SWIPE, HandEvent.Type.RIGHT_SWIPE,
                        HandEvent.Type.UP_SWIPE, HandEvent.Type.DOWN_SWIPE,
                        HandEvent.Type.THUMB_UP);
    }
}
