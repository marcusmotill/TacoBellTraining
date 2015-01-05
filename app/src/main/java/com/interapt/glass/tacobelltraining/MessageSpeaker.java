package com.interapt.glass.tacobelltraining;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by mikenguyen on 12/27/14.
 */
public class MessageSpeaker implements TextToSpeech.OnInitListener {
    private TextToSpeech textToSpeech;
    private boolean ready = false;
    private boolean allowed = false;

    public MessageSpeaker(Context context){
        textToSpeech = new TextToSpeech(context, this);
    }

    @Override
    public  void onInit(int status){
        if(status == TextToSpeech.SUCCESS){
            textToSpeech.setLanguage(Locale.US);
            ready = true;
        } else {
            ready = false;
        }
    }

    //Prompt 1 = "No speech input detected, please select a menu item, by saying, number,
    // then say the menu item number, for example, number, one"
    public void speakPrompt(String prompt){
        if(allowed && ready){
            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
            textToSpeech.speak(prompt, TextToSpeech.QUEUE_ADD, hashMap);
        }
    }

    public void pauseSpeaking(int duration){
        textToSpeech.playSilence(duration, TextToSpeech.QUEUE_ADD, null);
    }

    public void destroy(){
        textToSpeech.shutdown();
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed){
        this.allowed = allowed;
    }
}
