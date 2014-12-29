package com.interapt.mikenguyen.tacobelltraining;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.view.WindowUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TestActivity extends Activity implements SurfaceHolder.Callback {

    private Camera camera;
    private SurfaceView cameraPreview;
    private MediaRecorder mediaRecorder;
    private static int currentFoodItemNumber = 0;
    private static String employeeId = "";
    private FrameLayout cameraPreviewFrameLayout;
    private TextView recordingPromptTextView;
    private TextView prepTitleTextView;
    private TextView prepContentTextView;
    private TextView countDownTextView;
    private ImageView timerImageView;
    private CountDownTimer countDownTimer1;
    private CountDownTimer countDownTimer2;
    private static Context context;
    private final static int maximumWaitTimeForCamera = 5000;
    private final static String TAG = "RecordActivity";
    private static String outputFile = "";
    private boolean isRecording = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Requests a voice menu on this activity.
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        super.onCreate(savedInstanceState);
        Intent mIntent = getIntent();
        currentFoodItemNumber = mIntent.getIntExtra("currentFoodItemNumber", 0);
        employeeId = mIntent.getStringExtra("employeeId");
        String timeStamp = new SimpleDateFormat("MM_dd_yyyy").format(new Date());
        outputFile = "/data/data/com.interapt.mikenguyen.tacobelltraining/files/" + employeeId + "_" + String.valueOf(currentFoodItemNumber) + "_" + timeStamp + ".mp4";
        context = this.getApplicationContext();
        setContentView(R.layout.activity_test);
        cameraPreviewFrameLayout = (FrameLayout) findViewById(R.id.camera_preview_framelayout);
        prepContentTextView = (TextView) findViewById(R.id.prep_content_textview);
        prepTitleTextView = (TextView) findViewById(R.id.prep_title_textview);
        recordingPromptTextView = (TextView) findViewById(R.id.recording_prompt_textview);
        timerImageView = (ImageView) findViewById(R.id.timer_imageview);

        countDownTimer1 =  new CountDownTimer(7000, 1000) {

            public void onTick(long millisUntilFinished) {
                int currentTime = (int)millisUntilFinished/1000;
                if(currentTime <= 3){
                    recordingPromptTextView.setTextColor(Color.parseColor("#99cc33"));
                    recordingPromptTextView.setText(String.valueOf(currentTime));
                }
            }

            public void onFinish() {
                //recordingPromptTextView.setText("");
                //recordingPromptTextView.setGravity(Gravity.RIGHT | Gravity.TOP);
                //int imageResource = getResources().getIdentifier("ic_rec", "drawable", getPackageName());
                //Drawable recordingIcon = getResources().getDrawable(imageResource);
                //timerImageView.setImageDrawable(recordingIcon);
                recordingPromptTextView.setText("GO!");
                startRecording();
            }
        };

        countDownTimer2 =  new CountDownTimer(48000, 1000) {

            public void onTick(long millisUntilFinished) {
                countDownTextView.setTextColor(Color.parseColor("#cc3333"));
                countDownTextView.setText(String.valueOf(millisUntilFinished/1000));
            }

            public void onFinish() {
                stopRecording();
            }
        };
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            getMenuInflater().inflate(R.menu.menu_test, menu);
            return true;
        }
        // Pass through to super to setup touch menu.
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_test, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            switch (item.getItemId()) {
                case R.id.go_forward_menu_item:
                    showRecordingPrompt();
                    break;
                default:
                    return true;
            }
            return true;
        }
        // Good practice to pass through to super if not handled
        return super.onMenuItemSelected(featureId, item);
    }

    public void startRecording(){
        cameraPreview = new SurfaceView(this);
        cameraPreview.getHolder().addCallback(this);
        cameraPreviewFrameLayout.addView(cameraPreview);
        countDownTextView = new TextView(this);
        FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params1.gravity = Gravity.CENTER | Gravity.TOP;
        countDownTextView.setLayoutParams(params1);
        countDownTextView.setTextColor(Color.parseColor("#FFFFFF"));
        countDownTextView.setTextSize(30f);
        countDownTextView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        countDownTextView.setText("");
        countDownTextView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_rec, 0, 0, 0);
        cameraPreviewFrameLayout.addView(countDownTextView);
    }

    public void stopRecording() {
        mediaRecorder.stop();
        releaseMediaRecorder();
        releaseCamera();
        countDownTextView.setVisibility(View.INVISIBLE);
        showPostRecordingPrompts();
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void releaseCamera() {
        if (this.camera != null) {
            this.camera.stopPreview();
            this.camera.release();
            this.camera = null;
            this.cameraPreviewFrameLayout.removeView(this.cameraPreview);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if (camera == null) {
            camera = getCameraInstanceRetry();
        }

        Camera.Parameters params = camera.getParameters();
        params.setRecordingHint(true);
        camera.setParameters(params);

        try {
            camera.stopPreview();
            camera.setPreviewDisplay(null);
        } catch (IOException e) {
            Log.d(TAG, "IOException setting preview display: " + e.getMessage());

        }
        Log.d(TAG, "Here 1");
        camera.unlock();
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mediaRecorder.setOutputFile(outputFile);
        mediaRecorder.setPreviewDisplay(cameraPreview.getHolder().getSurface());

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
        }
        Log.d(TAG, "Here 2");
        mediaRecorder.start();
        countDownTimer2.start();
        isRecording = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //Do nothing
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //Do nothing
    }

    private Camera getCameraInstanceRetry() {
        Camera c = null;
        boolean acquiredCam = false;
        int timePassed = 0;
        while (!acquiredCam && timePassed < maximumWaitTimeForCamera) {
            try {
                c = Camera.open();
                acquiredCam = true;
                return c;
            } catch (Exception e) {
                Log.e(TAG, "Exception encountered opening camera:" + e.getLocalizedMessage());
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException ee) {
                Log.e(TAG, "Exception encountered sleeping:" + ee.getLocalizedMessage());
            }
            timePassed += 200;
        }
        return c;
    }

    // private void initGraphicalElement(){
    //     prepContentTextView = (TextView) findViewById(R.id.prep_content_textview);
    //     prepTitleTextView = (TextView) findViewById(R.id.prep_title_textview);
    //     recordingPromptTextView = (TextView) findViewById(R.id.recording_prompt_textview);
    //     timerImageView = (ImageView) findViewById(R.id.timer_imageview);
    //     // recordingPromptTextView = new TextView(this);
    //     // FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
    //     // params1.gravity = Gravity.CENTER;
    //     // recordingPromptTextView.setLayoutParams(params1);
    //     // recordingPromptTextView.setText("You have 48 seconds");
    //     // recordingPromptTextView.setTextColor(Color.parseColor("#FFFFFF"));
    //     // recordingPromptTextView.setTextSize(40f);
    //     // recordingPromptTextView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));

    //     // FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
    //     // params2.gravity = Gravity.CENTER | Gravity.TOP;
    //     // timerImageView = new ImageView(this);
    //     // timerImageView.setLayoutParams(params2);
    //     // int imageResource = getResources().getIdentifier("ic_timer", "drawable", getPackageName());
    //     // Drawable timerIcon = getResources().getDrawable(imageResource);
    //     // timerImageView.setMaxHeight(80);
    //     // timerImageView.setMaxWidth(80);
    //     // timerImageView.setImageDrawable(timerIcon);
    // }

    private void showRecordingPrompt(){
        prepContentTextView.setVisibility(View.INVISIBLE);
        prepTitleTextView.setVisibility(View.INVISIBLE);
        recordingPromptTextView.setVisibility(View.VISIBLE);
        timerImageView.setVisibility(View.VISIBLE);
        countDownTimer1.start();
    }

    private void showPostRecordingPrompts(){

    }
}