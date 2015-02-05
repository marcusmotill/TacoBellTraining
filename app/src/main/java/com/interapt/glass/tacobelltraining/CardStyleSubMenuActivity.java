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
public class CardStyleSubMenuActivity extends Activity implements Ari.StartCallback, Ari.ErrorCallback,
        HandEvent.Listener {

    /**
     * {@link CardScrollView} to use as the main content view.
     */
    private CardScrollView mCardScroller;
    private View trainingView;
    private View testView;
    private View[] viewArray;
    private View mView;
    int currentFoodItemNumber = 0;
    private ActiveAri mAri;
    private int currentPosition = 0;
    private AudioManager audioManager;
    private static final String TAG = "Card Style Main Menu";
    private static final String LEFT_SWIPE = "LEFT_SWIPE";
    private static final String RIGHT_SWIPE = "RIGHT_SWIPE";
    private static final String CLOSED_HAND = "CLOSED_HAND";
    private static final String DOWN_SWIPE = "DOWN_SWIPE";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent mIntent = getIntent();
        currentFoodItemNumber = mIntent.getIntExtra("menuItemNumber", 0);
        buildViews();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(new CardScrollAdapter() {
            @Override
            public int getCount() {
                return 2;
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
                int position = -1;
                for(int i = 0; i < viewArray.length; i++){
                    if(viewArray[i] == item){
                        position = i;
                    }
                }
                return position;
            }
        });
        // Handle the TAP event.
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Plays disallowed sound to indicate that TAP actions are not supported.
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(Sounds.SUCCESS);
                switch(position){
                    case 0:
                        startTrainingActivity();
                        break;
                    case 1:
                        startTestActivity();
                        break;
                    default:
                        break;
                }
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
     * Builds a Glass styled "Hello World!" view using the {@link CardBuilder} class.
     */
    private void buildViews() {
        trainingView = new CardBuilder(getApplicationContext(), CardBuilder.Layout.MENU)
                .setText("Training")
                .setIcon(R.drawable.ic_document_50)
                .setFootnote(FoodItem.getFoodItemName(currentFoodItemNumber))
                .getView();
        testView = new CardBuilder(getApplicationContext(), CardBuilder.Layout.MENU)
                .setIcon(R.drawable.ic_action_time)
                .setText("Test")
                .setFootnote(FoodItem.getFoodItemName(currentFoodItemNumber))
                .getView();
        viewArray = new View[2];
        viewArray[0] = trainingView;
        viewArray[1] = testView;
    }

    private void startTrainingActivity(){
        Intent trainingIntent = new Intent(this, TrainingActivity.class);
        if(currentFoodItemNumber != 0) {
            trainingIntent.putExtra("currentFoodItemNumber", currentFoodItemNumber);
            startActivity(trainingIntent);
        }
    }

    private void startTestActivity(){
        Intent testIntent = new Intent(getApplicationContext(), TestActivity.class);
        if(currentFoodItemNumber != 0) {
            testIntent.putExtra("currentFoodItemNumber", currentFoodItemNumber);
            startActivity(testIntent);
        }
    }

    private void startActivityWithSelection(int selection){
        switch (selection){
            case 0:
                startTrainingActivity();
                break;
            case 1:
                startTestActivity();
                break;
            default:
                break;
        }
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
                playSuccessSound();
                startActivityWithSelection(mCardScroller.getSelectedItemPosition());
                break;
            case DOWN_SWIPE:
                stopAri();
                playSuccessSound();
                finish();
                break;
        }
    }

    private void moveCursor(String eventType) {
        if (eventType.equals(RIGHT_SWIPE)) { // move right
            if(currentPosition < 1){
                currentPosition++;
            } else {
                currentPosition = 0;
            }
            mCardScroller.setSelection(currentPosition);
        } else if (eventType.equals(LEFT_SWIPE)) { // move left
            if(currentPosition > 0){
                currentPosition--;
            } else {
                currentPosition = 1;
            }
            mCardScroller.setSelection(currentPosition);
        }
    }

    @Override
    public void onAriStart() {
        // Enabling and disabling gestures is only available with Indie Developer and
        // Enterprise licenses.
        mAri.disable(HandEvent.Type.SWIPE_PROGRESS, HandEvent.Type.THUMB_UP,
                HandEvent.Type.UP_SWIPE, HandEvent.Type.OPEN_HAND)
                .enable(HandEvent.Type.CLOSED_HAND, HandEvent.Type.DOWN_SWIPE,
                        HandEvent.Type.LEFT_SWIPE, HandEvent.Type.RIGHT_SWIPE);
    }

    @Override
    public void onAriError(@Nonnull final Throwable throwable) {
        final String msg = "Ari error";
        Log.e(TAG, msg, throwable);

    }

}
