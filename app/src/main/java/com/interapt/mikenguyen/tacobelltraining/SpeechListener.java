package com.interapt.mikenguyen.tacobelltraining;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by mikenguyen on 12/24/14.
 */
class  SpeechListener implements RecognitionListener
{
    private Drawable busyMicrophone;
    private Drawable readyMicrophone;
    private Drawable recordingMicrophone;
    private Context context;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private int noSpeechInputTimeOut;
    private MainMenuActivity mainMenuActivityActivity;
    private SubMenuActivity subMenuActivity;

    public SpeechListener(MainMenuActivity mainMenuActivity, Context context, SpeechRecognizer speechRecognizer, Intent intent)
    {
        this.mainMenuActivityActivity = mainMenuActivity;
        this.context =context;
        this.speechRecognizer =speechRecognizer;
        this.speechRecognizerIntent = intent;
        noSpeechInputTimeOut = 0;
        initMicIcon();
    }

    public SpeechListener(SubMenuActivity subMenuActivity, Context context, SpeechRecognizer speechRecognizer, Intent intent)
    {
        this.subMenuActivity = subMenuActivity;
        this.context =context;
        this.speechRecognizer =speechRecognizer;
        this.speechRecognizerIntent = intent;
        noSpeechInputTimeOut = 0;
        initMicIcon();
    }
    public void onReadyForSpeech(Bundle params)
    {
        setMicrophoneIcon(readyMicrophone);
        hideLoadingAnimation();
        Log.d("Speech Listener", "ready to listen");
        //Log.d(TAG, "onReadyForSpeech");
    }
    public void onBeginningOfSpeech()
    {
        setMicrophoneIcon(recordingMicrophone);
        Log.d("Speech Listener", "start listening");
        //Log.d(TAG, "onBeginningOfSpeech");
    }
    public void onRmsChanged(float rmsdB)
    {
        //Log.d(TAG, "onRmsChanged");
    }
    public void onBufferReceived(byte[] buffer)
    {
        //Log.d(TAG, "onBufferReceived");
    }
    public void onEndOfSpeech()
    {
        Log.d("Speech Listener", "finish listening");
        speechRecognizer.startListening(speechRecognizerIntent);
    }
    public void onError(int error)
    {
        Log.d("Speech error",  "error " +  error);
        //7 -No recognition result matched.
        //9 - vInsufficient permissions
        //6 - No speech input
        //8 RecognitionService busy.
        //5 Other client side errors.
        //3 Audio recording error.
        //  mText.setText("error " + error);
        switch (error){
            case 1:case 2:
                setMicrophoneIcon(busyMicrophone);
                break;
            case 6:
                if(noSpeechInputTimeOut < 1){
                    noSpeechInputTimeOut++;
                    restartSpeechRecognition();
                } else {
                    playPrompt();
                    restartSpeechRecognition();
                    noSpeechInputTimeOut = 0;
                }
                break;
            case 7:
                //no matching results
                break;
            case 8:
                setMicrophoneIcon(busyMicrophone);
                showLoadingAnimation();
                break;
            case 3:case 4:case 5:
                restartSpeechRecognition();
                break;
            default:
                break;
        }
    }
    public void onResults(Bundle results)
    {
        //Log.v(TAG,"onResults" + results);
        ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Log.d("Speech listener", "result: " + data.get(0));
        parseSpeechResult(data.get(0).toString());
        restartSpeechRecognition();
    }
    public void onPartialResults(Bundle partialResults)
    {
        ArrayList data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        setPartialSpeechResult(data.get(0).toString());
    }

    public void onEvent(int eventType, Bundle params)
    {
        //Log.d(TAG, "onEvent " + eventType);
    }

    private void initMicIcon(){
        int imageResourceReadyMic = context.getResources().getIdentifier("ic_mic_ready", "drawable", context.getPackageName());
        int imageResourceBusyMic = context.getResources().getIdentifier("ic_mic_busy", "drawable", context.getPackageName());
        int imageResourceRecordingMic = context.getResources().getIdentifier("ic_mic_recording", "drawable", context.getPackageName());
        readyMicrophone = context.getResources().getDrawable(imageResourceReadyMic);
        busyMicrophone = context.getResources().getDrawable(imageResourceBusyMic);
        recordingMicrophone = context.getResources().getDrawable(imageResourceRecordingMic);
    }

    private void setMicrophoneIcon(Drawable micIcon){
        if(mainMenuActivityActivity != null){
            mainMenuActivityActivity.setMicImageView(micIcon);
        } else {
            //subMenuActivity.setMicImageView(micIcon);
        }
    }

    private void playPrompt(){
        Log.d("Speech listener", "No speech input!");
    }

    private void restartSpeechRecognition(){
        speechRecognizer.cancel();
        speechRecognizer.startListening(speechRecognizerIntent);
    }

    private void showLoadingAnimation(){
        if(mainMenuActivityActivity != null) {
            mainMenuActivityActivity.showLoadingAnimation();
        } else {
            //subMenuActivity.showLoadingAnimation();
        }
    }

    private void hideLoadingAnimation(){
        if(mainMenuActivityActivity != null) {
            mainMenuActivityActivity.hideLoadingAnimation();
        } else {
            //subMenuActivity.hideLoadingAnimation();
        }
    }

    private void setPartialSpeechResult(String partialResult) {
        if(mainMenuActivityActivity != null) {
            mainMenuActivityActivity.setPartialSpeechResult(partialResult);
        } else {
            //subMenuActivity.setPartialSpeechResult(partialResult);
        }
    }

    private void parseSpeechResult(String speechResult){
        switch (speechResult) {
            case "number one":case "number 1":case "1":
                selectMenuItem(1);
                break;
            case "number two":case "number 2":case "2":
                selectMenuItem(2);
                break;
            case "number three":case "number 3":case "3":
                selectMenuItem(3);
                break;
            case "number four":case "number 4":case "4":
                selectMenuItem(4);
                break;
            case "number five":case "number 5":case "5":
                selectMenuItem(5);
                break;
            default:
                //wrong speech input, play shake animation
                showShakeAnimation();
                break;
        }
    }

    private void selectMenuItem(int menuNumber){
        if(mainMenuActivityActivity != null) {
            mainMenuActivityActivity.startSubMenuActivity(menuNumber);
        } else {
            //subMenuActivity.startSubMenuActivity(menuNumber);
        }
    }

    private void showShakeAnimation(){
        if(mainMenuActivityActivity != null) {
            mainMenuActivityActivity.playDisallowedSound();
        } else {
            //subMenuActivity.playDisallowedSound();
        }
    }
}
