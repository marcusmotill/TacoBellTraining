package com.interapt.glass.tacobelltraining;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

/**
 * Created by mikenguyen on 1/6/15.
 */
public class TextSpeaker {

    private TextToSpeech textToSpeech;
    private Context currentContext;
    private String currentActivity;
    private boolean isSpeaking;
    private boolean isFinishSpeaking;

    public TextSpeaker(Context context, String whichActivity){
        currentContext = context;
        currentActivity = whichActivity;
        isFinishSpeaking = false;
        isSpeaking = false;
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });
        textToSpeech.setLanguage(Locale.US);
    }

    public void destroy(){
        textToSpeech.shutdown();
    }

    public boolean isSpeaking() {
        return textToSpeech.isSpeaking();
    }

    public void stopSpeaking(){
        textToSpeech.stop();
    }

    public void speakMessage(int whichMessage){
        String messageToSpeak = "";
        switch(currentActivity){
            case "MainMenuActivity":
                messageToSpeak = getMainMenuMessage(whichMessage);
                break;
            case "SubMenuActivity":
                messageToSpeak = getSubMenuMessage(whichMessage);
                break;
            case "TrainingActivity":
                messageToSpeak = getTrainingMessage(whichMessage);
                break;
            case "TestActivity":
                messageToSpeak = getTestMessage(whichMessage);
                break;
            default:
                break;
        }
        textToSpeech.speak(messageToSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }

    private String getMainMenuMessage (int whichMessage){
        String messageToSpeak = "";
        switch(whichMessage){
            case 1:
                messageToSpeak = "Speak the menu item number, or speak the food name to start.";
                break;
        }
        return messageToSpeak;
    }

    private String getSubMenuMessage (int whichMessage){
        String messageToSpeak = "";
        switch(whichMessage){
            case 1:
                messageToSpeak = "Speak the menu item number, or speak the food name to start.";
                break;
        }
        return messageToSpeak;
    }

    private String getTrainingMessage (int whichMessage){
        String messageToSpeak = "";
        switch(whichMessage){
            case 11:
                messageToSpeak = "Step 1: Regrill Bolillo FlatBread";
                Log.i("Text Speaker", "Speaking step 1");
                break;
            case 12:
                messageToSpeak = "Step 2: Fold and place on wrap, bubble side down";
                break;
            case 13:
                messageToSpeak = "Step 3: Add 3 portions of steak";
                break;
            case 14:
                messageToSpeak = "Key step: Drain liquid from steak";
                break;
            case 15:
                messageToSpeak = "Key step: 3 times the portion";
                break;
            case 16:
                messageToSpeak = "Key step: Use clear bag, marked braised shaved steak";
                break;
            case 17:
                messageToSpeak = "Step 4: Add 2 portions of 3-cheese blend";
                break;
            case 18:
                messageToSpeak = "Step 5: Slide onto paddle, closed side first";
                break;
            case 19:
                messageToSpeak = "Step 6: Melt for a single cycle";
                break;
            case 110:
                messageToSpeak = "Target: 10 point 4 ounces";
                break;
            case 1000:
                messageToSpeak =  "Training Complete, say ok glass, go forward to proceed to testing, " +
                        "or, say ok glass, try again to review training";
                break;
        }
        return messageToSpeak;
    }

    private String getTestMessage (int whichMessage){
        String messageToSpeak = "";
        switch(whichMessage){
            case 1:
                messageToSpeak = "Speak the menu item number, or speak the food name to start.";
                break;
        }
        return messageToSpeak;
    }

    public TextToSpeech getTextToSpeechObject(){
        return textToSpeech;
    }
}
