package com.interapt.glass.tacobelltraining;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
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
    private GetIdActivity getIdActivity;
    private String parentActivityName;
    private final String MIC_PROMPT_READY = "Speak Now";
    private final String MIC_PROMPT_LISTENING = "Listening";
    private final String MIC_PROMPT_PROCESSING = "Processing";

    public SpeechListener(MainMenuActivity mainMenuActivity, Context context, SpeechRecognizer speechRecognizer, Intent intent)
    {
        this(context, speechRecognizer, intent);
        this.mainMenuActivityActivity = mainMenuActivity;
        parentActivityName = "MainMenuActivity";
    }

    public SpeechListener(SubMenuActivity subMenuActivity, Context context, SpeechRecognizer speechRecognizer, Intent intent)
    {
        this(context, speechRecognizer, intent);
        this.subMenuActivity = subMenuActivity;
        parentActivityName = "SubMenuActivity";
    }

    public SpeechListener(GetIdActivity getIdActivity, Context context, SpeechRecognizer speechRecognizer, Intent intent){
        this(context, speechRecognizer, intent);
        this.getIdActivity = getIdActivity;
        parentActivityName = "GetIdActivity";
    }

    public SpeechListener(Context context, SpeechRecognizer speechRecognizer, Intent intent){
        this.context =context;
        this.speechRecognizer =speechRecognizer;
        this.speechRecognizerIntent = intent;
        noSpeechInputTimeOut = 0;
        initMicIcon();
    }
    public void onReadyForSpeech(Bundle params)
    {
        setMicPromptMessage(MIC_PROMPT_READY);
        setMicrophoneIcon(readyMicrophone);
        hideLoadingAnimation();
        Log.d("Speech Listener", "ready to listen");
        //Log.d(TAG, "onReadyForSpeech");
    }
    public void onBeginningOfSpeech()
    {
        setMicPromptMessage(MIC_PROMPT_LISTENING);
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
                setMicPromptMessage(MIC_PROMPT_PROCESSING);
                break;
            case 6:
                if(!isOnline()) {
                    startWifiSetting();
                }
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
                setMicPromptMessage(MIC_PROMPT_PROCESSING);
                setMicrophoneIcon(recordingMicrophone);
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
        switch(parentActivityName){
            case "MainMenuActivity":
                mainMenuActivityActivity.setMicImageView(micIcon);
                break;
            case "SubMenuActivity":
                subMenuActivity.setMicImageView(micIcon);
                break;
            case "GetIdActivity":
                getIdActivity.setMicImageView(micIcon);
                break;
            default:
                break;
        }
    }

    private void restartSpeechRecognition(){
        speechRecognizer.cancel();
        speechRecognizer.startListening(speechRecognizerIntent);
    }

    private void showLoadingAnimation(){
        switch(parentActivityName){
            case "MainMenuActivity":
                mainMenuActivityActivity.showLoadingAnimation();
                break;
            case "SubMenuActivity":
                subMenuActivity.showLoadingAnimation();
                break;
            case "GetIdActivity":
                getIdActivity.showLoadingAnimation();
                break;
            default:
                break;
        }
    }

    private void hideLoadingAnimation(){
        switch(parentActivityName){
            case "MainMenuActivity":
                mainMenuActivityActivity.hideLoadingAnimation();
                break;
            case "SubMenuActivity":
                subMenuActivity.hideLoadingAnimation();
                break;
            case "GetIdActivity":
                getIdActivity.hideLoadingAnimation();
                break;
            default:
                break;
        }
    }

    private void setPartialSpeechResult(String partialResult) {
        switch(parentActivityName){
            case "MainMenuActivity":
                mainMenuActivityActivity.setPartialSpeechResult(partialResult);
                break;
            case "SubMenuActivity":
                subMenuActivity.setPartialSpeechResult(partialResult);
                break;
            case "GetIdActivity":
                break;
            default:
                break;
        }
    }

    private void parseSpeechResult(String speechResult){
        switch(parentActivityName){
            case "MainMenuActivity":
                switch (speechResult) {
                    case "number one":case "number 1":case "1":case "one":case "triple steak stack":
                        selectMenuItem(1);
                        break;
                    case "number two":case "number 2":case "2":case "two":case "chicken triple steak stack":
                        selectMenuItem(2);
                        break;
                    case "number three":case "number 3":case "3":case "three":case "Cinnabon coffee":case "cinnabon coffee":
                        selectMenuItem(3);
                        break;
                    case "number four":case "number 4":case "4":case "four":case "iced coffee":
                        selectMenuItem(4);
                        break;
                    case "number five":case "number 5":case "5":case "five":case "cheesy burrito":
                        selectMenuItem(5);
                        break;
                    default:
                        //wrong speech input, play disallowed
                        playDisallowed();
                        break;
                }
                break;
            case "SubMenuActivity":
                switch (speechResult) {
                    case "number one":case "number 1":case "1":case "one":case "training":
                        selectMenuItem(1);
                        break;
                    case "number two":case "number 2":case "2":case "two":case "test":
                        selectMenuItem(2);
                        break;
                    case "number three":case "number 3":case "3":case "three":case "back to main menu":
                        selectMenuItem(3);
                        break;
                    default:
                        //wrong speech input, play disallowed
                        playDisallowed();
                        break;
                }
                break;
            case "GetIdActivity":
                if(getIdActivity.isGotUserId()){
                    switch (speechResult){
                        case "yes":case "Yes":
                            selectMenuItem(1);
                            break;
                        case "no":case "No":
                            selectMenuItem(2);
                            break;
                        default:
                            //wrong speech input, play disallowed
                            playDisallowed();
                            break;
                    }
                } else {
                    getIdActivity.parseUserId(speechResult);
                }

                break;
            default:
                break;
        }
    }

    private void selectMenuItem(int menuNumber){
        switch(parentActivityName){
            case "MainMenuActivity":
                mainMenuActivityActivity.startSubMenuActivity(menuNumber);
                break;
            case "SubMenuActivity":
                subMenuActivity.selectMenuItem(menuNumber);
                break;
            case "GetIdActivity":
                getIdActivity.selectMenuItem(menuNumber);
                break;
            default:
                break;
        }
    }

    private void playDisallowed(){
        switch(parentActivityName){
            case "MainMenuActivity":
                mainMenuActivityActivity.playDisallowedSound();
                break;
            case "SubMenuActivity":
                subMenuActivity.playDisallowedSound();
                break;
            case "GetIdActivity":
                getIdActivity.playDisallowedSound();
                break;
            default:
                break;
        }
    }

    private void setMicPromptMessage(String message){
        switch(parentActivityName){
            case "MainMenuActivity":
                mainMenuActivityActivity.setMicPromptMessage(message);
                break;
            case "SubMenuActivity":
                subMenuActivity.setMicPromptMessage(message);
                break;
//            case "GetIdActivity":
//                getIdActivity.setMicPromptMessage(message);
//                break;
            default:
                break;
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void startWifiSetting(){
        Intent wifiIntent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        switch(parentActivityName){
            case "MainMenuActivity":
                mainMenuActivityActivity.startActivity(wifiIntent);
                break;
            case "SubMenuActivity":
                subMenuActivity.startActivity(wifiIntent);
                break;
            case "GetIdActivity":
                getIdActivity.startActivity(wifiIntent);
                break;
            default:
                break;
        }
    }
}
