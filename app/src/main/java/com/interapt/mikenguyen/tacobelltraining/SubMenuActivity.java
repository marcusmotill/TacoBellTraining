package com.interapt.mikenguyen.tacobelltraining;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.glass.view.WindowUtils;


public class SubMenuActivity extends Activity {
    private static final int SPEECH_REQUEST = 0;
    private  static int currentFoodItemNumber = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Requests a voice menu on this activity.
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        super.onCreate(savedInstanceState);
        Intent mIntent = getIntent();
        currentFoodItemNumber = mIntent.getIntExtra("menuItemNumber", 0);
        Log.d("menu item number: ", String.valueOf(currentFoodItemNumber));
        setContentView(R.layout.activity_sub_menu);
    }


    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            getMenuInflater().inflate(R.menu.menu_sub_menu, menu);
            return true;
        }
        // Pass through to super to setup touch menu.
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sub_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            switch (item.getItemId()) {
                case R.id.one_menu_item:
                    Intent trainingIntent = new Intent(this, TrainingActivity.class);
                    if(currentFoodItemNumber != 0) {
                        trainingIntent.putExtra("currentFoodItemNumber", currentFoodItemNumber);
                        startActivity(trainingIntent);
                    }
                    break;
                case R.id.two_menu_item:
                    Intent testIntent = new Intent(this, TestActivity.class);
                    if(currentFoodItemNumber != 0) {
                        testIntent.putExtra("currentFoodItemNumber", currentFoodItemNumber);
                        startActivity(testIntent);
                    }
                    break;
                case R.id.three_menu_item:
                    finish();
                    break;
                default:
                    return true;
            }
            return true;
        }
        // Good practice to pass through to super if not handled
        return super.onMenuItemSelected(featureId, item);
    }
}
