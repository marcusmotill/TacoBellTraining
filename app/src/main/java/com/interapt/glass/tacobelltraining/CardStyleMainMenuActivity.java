package com.interapt.glass.tacobelltraining;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.os.Handler;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import javax.annotation.Nonnull;

import io.onthego.ari.KeyDecodingException;
import io.onthego.ari.android.ActiveAri;
import io.onthego.ari.android.Ari;
import io.onthego.ari.event.HandEvent;

/**
 * An {@link Activity} showing a tuggable "Hello World!" card.
 * <p/>
 * The main content view is composed of a one-card {@link CardScrollView} that provides tugging
 * feedback to the user when swipe gestures are detected.
 * If your Glassware intends to intercept swipe gestures, you should set the content view directly
 * and use a {@link com.google.android.glass.touchpad.GestureDetector}.
 *
 * @see <a href="https://developers.google.com/glass/develop/gdk/touch">GDK Developer Guide</a>
 */
public class CardStyleMainMenuActivity extends Activity implements Ari.StartCallback, Ari.ErrorCallback,
        HandEvent.Listener {

    /**
     * {@link CardScrollView} to use as the main content view.
     */
    private CardScrollView mCardScroller;
    private View mView1;
    private View mView2;
    private View mView3;
    private View mView4;
    private View mView5;
    private View[] viewArray;
    private ActiveAri mAri;
    private int currentPosition = 0;
    private AudioManager audioManager;
    private static final String TAG = "Card Style Main Menu";
    private static final String LEFT_SWIPE = "LEFT_SWIPE";
    private static final String RIGHT_SWIPE = "RIGHT_SWIPE";
    private static final String CLOSED_HAND = "CLOSED_HAND";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        buildViews();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(new CardScrollAdapter() {
            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public Object getItem(int position) {
                return viewArray[position];
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return viewArray[position];
            }

            @Override
            public int getPosition(Object item) {
                return 0;
            }
        });
        // Handle the TAP event.
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startSubMenuActivity();
            }
        });
        setContentView(mCardScroller);
//        try {
//            mAri = ActiveAri.getInstance(getString(R.string.ari_license_key), this)
//                    .addListeners(this)
//                    .addErrorCallback(this);
//        } catch (final KeyDecodingException e) {
//            Log.e(TAG, "Failed to init Ari: ", e);
//            finish();
//        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startSubMenuActivity();
            }
        }, 2500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
//        if(mAri != null){
//            mAri.start(this);
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCardScroller.deactivate();
//        stopAri();
    }

    /**
     * Builds a Glass styled main menu view using the {@link CardBuilder} class.
     */
    private void buildViews() {
                mView1 = new CardBuilder(getApplicationContext(), CardBuilder.Layout.CAPTION)
                .setText("Sriracha Quesarito")
                .setFootnote("")
                .setTimestamp("")
                .addImage(R.drawable.menu_6)
                .getView();
//        mView1 = new CardBuilder(getApplicationContext(), CardBuilder.Layout.CAPTION)
//                .setText("Triple Steak Stack")
//                .setFootnote("Tap to select")
//                .setTimestamp("Swipe to navigate")
//                .addImage(R.drawable.menu_1)
//                .getView();
//        mView2 = new CardBuilder(getApplicationContext(), CardBuilder.Layout.CAPTION)
//                .setText("Chicken Triple Steak Stack")
//                .setFootnote("Tap to select")
//                .setTimestamp("Swipe to navigate")
//                .addImage(R.drawable.menu_2)
//                .getView();
//        mView3 = new CardBuilder(getApplicationContext(), CardBuilder.Layout.CAPTION)
//                .setText("Cinnabon Coffee")
//                .setFootnote("Tap to select")
//                .setTimestamp("Swipe to navigate")
//                .addImage(R.drawable.menu_3)
//                .getView();
//        mView4 = new CardBuilder(getApplicationContext(), CardBuilder.Layout.CAPTION)
//                .setText("Iced Coffee")
//                .setFootnote("Tap to select")
//                .setTimestamp("Swipe to navigate")
//                .addImage(R.drawable.menu_4)
//                .getView();
//        mView5 = new CardBuilder(getApplicationContext(), CardBuilder.Layout.CAPTION)
//                .setText("Cheesy Burrito")
//                .setFootnote("Tap to select")
//                .setTimestamp("Swipe to navigate")
//                .addImage(R.drawable.menu_5)
//                .getView();
        viewArray = new View[1];
        viewArray[0] = mView1;
//        viewArray[1] = mView2;
//        viewArray[2] = mView3;
//        viewArray[3] = mView4;
//        viewArray[4] = mView5;
    }

    private void startSubMenuActivity(){
        //playSuccessSound();
        Intent myIntent = new Intent(getApplicationContext(), CardStyleSubMenuActivity.class);
        myIntent.putExtra("menuItemNumber", 6);
        startActivity(myIntent);
    }

    private void playSuccessSound(){
        // Plays success sound to indicate that TAP actions were received.
        audioManager.playSoundEffect(Sounds.SUCCESS);
    }

    private void stopAri(){
        if(mAri!=null){
            mAri.stop();
        }
    }

    @Override
    public void onHandEvent(HandEvent handEvent) {
        Log.i(TAG, "Ari " + handEvent.type);
        String eventType = handEvent.type.toString();
        switch(eventType){
            case RIGHT_SWIPE:case LEFT_SWIPE:
                moveCursor(eventType);
                break;
            case CLOSED_HAND:
                stopAri(); //to eliminate selecting twice
                startSubMenuActivity();
                break;
            default:
                break;
        }
    }

    private void moveCursor(String eventType) {
        if (eventType.equals(LEFT_SWIPE)) { // move left
            if(currentPosition < 4){
                currentPosition++;
            } else {
                currentPosition = 0;
            }
            mCardScroller.setSelection(currentPosition);
        } else if (eventType.equals(RIGHT_SWIPE)) { // move right
            if(currentPosition > 0){
                currentPosition--;
            } else {
                currentPosition = 4;
            }
            mCardScroller.setSelection(currentPosition);
        }
    }

    @Override
    public void onAriStart() {
        // Enabling and disabling gestures is only available with Indie Developer and
        // Enterprise licenses.
        mAri.disable(HandEvent.Type.SWIPE_PROGRESS, HandEvent.Type.THUMB_UP,
                     HandEvent.Type.UP_SWIPE, HandEvent.Type.DOWN_SWIPE, HandEvent.Type.OPEN_HAND)
                .enable(HandEvent.Type.CLOSED_HAND,
                        HandEvent.Type.LEFT_SWIPE, HandEvent.Type.RIGHT_SWIPE);
    }

    @Override
    public void onAriError(@Nonnull final Throwable throwable) {
        final String msg = "Ari error";
        Log.e(TAG, msg, throwable);
    }
}
