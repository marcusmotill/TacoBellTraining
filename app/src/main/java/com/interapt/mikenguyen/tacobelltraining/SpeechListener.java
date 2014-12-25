package com.interapt.mikenguyen.tacobelltraining;

import android.content.Context;
import android.content.Intent;
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
    Context context1;
    SpeechRecognizer sr;
    Intent intent;
    public SpeechListener(Context context, SpeechRecognizer speechRecognizer, Intent mIntent)
    {
        //Log.i("onError startListening","enter"+"nam");
        context1=context;
        sr=speechRecognizer;
        intent = mIntent;
    }
    public void onReadyForSpeech(Bundle params)
    {
        Log.d("Speech Listener", "ready to listen");
        //Log.d(TAG, "onReadyForSpeech");
    }
    public void onBeginningOfSpeech()
    {
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
        sr.startListening(intent);
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

        if(error==6 || error==7 || error==4  || error==1 || error==2 || error==5 || error==3 || error==8 || error==9 )
        {
            sr.startListening(intent);
            //Log.i("onError startListening","onError startListening"+error);
        }
    }
    public void onResults(Bundle results)
    {
        //Log.v(TAG,"onResults" + results);
        ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        for (int i = 0; i < data.size(); i++)
        {
            Log.d("Speech listener", "result: " + data.get(i));
            //str += data.get(i);

            //Toast.makeText(context1, "results: "+data.get(0).toString(), Toast.LENGTH_LONG).show();
            //Log.v("my", "output"+"results: "+data.get(0).toString());

            //sr.startListening(intent);
        }
    }
    public void onPartialResults(Bundle partialResults)
    {
        ArrayList data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        for (int i = 0; i < data.size(); i++)
        {
            Log.d("Speech listener", "partial result: " + data.get(i));
            //str += data.get(i);

            //Toast.makeText(context1, "results: "+data.get(0).toString(), Toast.LENGTH_LONG).show();
            //Log.v("my", "output"+"results: "+data.get(0).toString());

            //sr.startListening(intent);
        }
    }
    public void onEvent(int eventType, Bundle params)
    {
        //Log.d(TAG, "onEvent " + eventType);
    }
}
