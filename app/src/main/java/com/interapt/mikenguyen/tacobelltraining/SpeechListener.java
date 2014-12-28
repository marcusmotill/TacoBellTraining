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
    private String parentActivityName;

    public SpeechListener(MainMenuActivity mainMenuActivity, Context context, SpeechRecognizer speechRecognizer, Intent intent)
    {
        this.mainMenuActivityActivity = mainMenuActivity;
        this.context =context;
        this.speechRecognizer =speechRecognizer;
        this.speechRecognizerIntent = intent;
        noSpeechInputTimeOut = 0;
        parentActivityName = "MainMenuActivity";
        initMicIcon();
    }

    public SpeechListener(SubMenuActivity subMenuActivity, Context context, SpeechRecognizer speechRecognizer, Intent intent)
    {
        this.subMenuActivity = subMenuActivity;
        this.context =context;
        this.speechRecognizer =speechRecognizer;
        this.speechRecognizerIntent = intent;
        noSpeechInputTimeOut = 0;
        parentActivityName = "SubMenuActivity";
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
        if(parentActivityName == "MainMenuActivity"){
            mainMenuActivityActivity.setMicImageView(micIcon);
        } else {
            subMenuActivity.setMicImageView(micIcon);
        }
    }

    private void restartSpeechRecognition(){
        speechRecognizer.cancel();
        speechRecognizer.startListening(speechRecognizerIntent);
    }

    private void showLoadingAnimation(){
        if(parentActivityName == "MainMenuActivity") {
            mainMenuActivityActivity.showLoadingAnimation();
        } else {
            subMenuActivity.showLoadingAnimation();
        }
    }

    private void hideLoadingAnimation(){
        if(parentActivityName == "MainMenuActivity") {
            mainMenuActivityActivity.hideLoadingAnimation();
        } else {
            subMenuActivity.hideLoadingAnimation();
        }
    }

    private void setPartialSpeechResult(String partialResult) {
        if(parentActivityName == "MainMenuActivity") {
            mainMenuActivityActivity.setPartialSpeechResult(partialResult);
        } else {
            subMenuActivity.setPartialSpeechResult(partialResult);
        }
    }

    private void parseSpeechResult(String speechResult){
        if(parentActivityName == "MainMenuActivity"){
            switch (speechResult) {
                case "number one":case "number 1":case "1":case "one":case "triple steak stack":
                    selectMenuItem(1);
                    break;
                case "number two":case "number 2":case "2":case "chicken triple steak stack":
                    selectMenuItem(2);
                    break;
                case "number three":case "number 3":case "3":case "Cinnabon coffee":case "cinnabon coffee":
                    selectMenuItem(3);
                    break;
                case "number four":case "number 4":case "4":case "iced coffee":
                    selectMenuItem(4);
                    break;
                case "number five":case "number 5":case "5":case "cheesy burrito":
                    selectMenuItem(5);
                    break;
                default:
                    //wrong speech input, play shake animation
                    showShakeAnimation();
                    break;
            }
        } else {
            switch (speechResult) {
                case "number one":case "number 1":case "1":case "training":
                    selectMenuItem(1);
                    break;
                case "number two":case "number 2":case "2":case "test":
                    selectMenuItem(2);
                    break;
                case "number three":case "number 3":case "3":case "back to main menu":
                    selectMenuItem(3);
                    break;
                default:
                    //wrong speech input, play shake animation
                    showShakeAnimation();
                    break;
            }
        }

    }

    private void selectMenuItem(int menuNumber){
        if(parentActivityName == "MainMenuActivity") {
            mainMenuActivityActivity.startSubMenuActivity(menuNumber);
        } else {
            subMenuActivity.selectMenuItem(menuNumber);
        }
    }

    private void showShakeAnimation(){
        //play disallowed sound for now
        if(parentActivityName == "MainMenuActivity") {
            mainMenuActivityActivity.playDisallowedSound();
        } else {
            subMenuActivity.playDisallowedSound();
        }
    }
}
