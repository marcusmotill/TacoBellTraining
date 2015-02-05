package com.interapt.glass.tacobelltraining;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import android.location.*;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.view.WindowUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TestActivity extends Activity implements SurfaceHolder.Callback, ConnectionCallbacks,
        OnConnectionFailedListener {

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
    private CountDownTimer countDownTimer1;
    private CountDownTimer countDownTimer2;
    private CountDownTimer countDownTimer3;
    private CountDownTimer countDownTimer4;
    private ImageView testIconImageView;
    private Drawable infoDrawable;
    private Drawable successDrawable;
    private Drawable timerDrawable;
    private static AudioManager audioManager;
    private int testTimeLimit;
    private static Context context;
    private final static int maximumWaitTimeForCamera = 5000;
    private final static String TAG = "RecordActivity";
    //private GoogleApiClient mGoogleApiClient;
    private static String videoOutputFile = "";
    private static String reportOutputFile = "";
    private boolean finishRecording = false;
    private boolean isRecording = false;
    private boolean hasSubmittedTest = false;
    private boolean isStop = false;
    private TestReport testReport;
    private GMailSender mailSender;
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private static final int REQUEST_CODE_CREATOR = 2;
    //private static final String uploadEmail = "zjuvmzyozea5@m.youtube.com";
    private static final String uploadEmail = "tacobelltrainingresults@gmail.com";
    private static final String gmailUsername = "tacobelltrainingresults@gmail.com";
    private static final String gmailPassword = "interapt";
    private static LocationManager locationManager;
    private static String currentLocationString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Requests a voice menu on this activity.
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        super.onCreate(savedInstanceState);
        Intent mIntent = getIntent();
        currentFoodItemNumber = mIntent.getIntExtra("currentFoodItemNumber", 0);
        testTimeLimit = FoodItem.getTestTimeLimit(currentFoodItemNumber);
        //employeeId = mIntent.getStringExtra("employeeId");
        employeeId = "SAMPLE";
        String timeStamp = new SimpleDateFormat("MM_dd_yyyy_hh_mm_ss").format(new Date());
        videoOutputFile = employeeId + "_" + String.valueOf(currentFoodItemNumber) + "_" + timeStamp + "_video.mp4";
        reportOutputFile = employeeId + "_" + String.valueOf(currentFoodItemNumber) + "_" + timeStamp + "_report.txt";
        context = this.getApplicationContext();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        testReport = new TestReport(employeeId, FoodItem.getFoodItemName(currentFoodItemNumber));
        mailSender = new GMailSender(gmailUsername, gmailPassword);
        setContentView(R.layout.activity_test);
        //initGoogleDrive();
        initUI();
        initCountDownTimer();
//        if(currentFoodItemNumber == 1 || currentFoodItemNumber == 2){
//            countDownTimer3.start();
//        } else {
//            showRecordingPrompt();
//        }
        countDownTimer4.start();
        currentLocationString = "";
        getLocation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isStop = false;
//        if (mGoogleApiClient == null) {
//            // Create the API client and bind it to an instance variable.
//            // We use this instance as the callback for connection and connection
//            // failures.
//            // Since no account name is passed, the user is prompted to choose.
//            mGoogleApiClient = new GoogleApiClient.Builder(this)
//                    .addApi(Drive.API)
//                    .addScope(Drive.SCOPE_FILE)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .build();
//        }
//        // Connect the client. Once connected, the camera is launched.
//        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
//        if (mGoogleApiClient != null) {
//            mGoogleApiClient.disconnect();
//        }
        isStop = true;
        if(isRecording) {
            stopRecording();
        }
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            if(finishRecording){
                getMenuInflater().inflate(R.menu.menu_test_finish_recording, menu);
            } else {
                if(isRecording){
                    getMenuInflater().inflate(R.menu.menu_test_recording, menu);
                }else {
                    getMenuInflater().inflate(R.menu.menu_test, menu);
                }
            }

            return true;
        }
        // Pass through to super to setup touch menu.
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(finishRecording){
            getMenuInflater().inflate(R.menu.menu_test_finish_recording, menu);
        } else {
            if(isRecording){
                getMenuInflater().inflate(R.menu.menu_test_recording, menu);
            }else {
                getMenuInflater().inflate(R.menu.menu_test, menu);
            }
        }
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            switch (item.getItemId()) {
//                case R.id.go_forward_menu_item:
//                    if(finishRecording && !hasSubmittedTest){
//                        if(isOnline()) {
//                            submitTestResult();
//                        } else {
//                            showNoInternetPrompts();
//                        }
//                    }
//                    break;
                case R.id.retry_menu_item:
                    if(isRecording){
                        stopRecording();
                    }
                    finishRecording = false;
                    showRecordingPrompt();
                    break;
                case R.id.stop_recording_menu_item:
                    stopRecording();
                    break;
                case R.id.dismiss_menu_item:
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

    public void playSuccessSound(){
        audioManager.playSoundEffect(Sounds.SUCCESS);
    }

    public void playTickSound(){
        audioManager.playSoundEffect(Sounds.SELECTED);
    }

    public void playDisallowedSound(){
        audioManager.playSoundEffect(Sounds.DISALLOWED);
    }

    public void startRecording(){
        testReport.incrementNumberOfTrials();
        cameraPreview = new SurfaceView(this);
        cameraPreview.getHolder().addCallback(this);
        cameraPreviewFrameLayout.addView(cameraPreview);
        cameraPreviewFrameLayout.addView(countDownTextView);
    }

    public void stopRecording() {
        mediaRecorder.stop();
        releaseMediaRecorder();
        releaseCamera();
        cameraPreviewFrameLayout.removeView(countDownTextView);
        isRecording = false;
        finishRecording = true;
        //showPostRecordingPrompts();
        //countDownTimer4.start();
        if(isOnline()) {
            submitTestResult();
        } else {
            showNoInternetPrompts();
        }
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
        camera.unlock();
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
        mediaRecorder.setOutputFile(getFile(videoOutputFile).toString());
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

    private void initCountDownTimer(){
        countDownTimer1 =  new CountDownTimer(6000, 1000) {

            public void onTick(long millisUntilFinished) {
                int currentTime = (int)millisUntilFinished/1000;
                if(currentTime <= 3 && !isStop){
                    recordingPromptTextView.setTextColor(Color.parseColor("#16b902"));
                    recordingPromptTextView.setText(String.valueOf(currentTime));
                    playTickSound();
                }
            }

            public void onFinish() {
                //recordingPromptTextView.setText("");
                //recordingPromptTextView.setGravity(Gravity.RIGHT | Gravity.TOP);
                //int imageResource = getResources().getIdentifier("ic_rec", "drawable", getPackageName());
                //Drawable recordingIcon = getResources().getDrawable(imageResource);
                //timerImageView.setImageDrawable(recordingIcon);
                if(!isStop) {
                    recordingPromptTextView.setVisibility(View.INVISIBLE);
                    playSuccessSound();
                    startRecording();
                }
            }
        };

        countDownTimer2 =  new CountDownTimer(testTimeLimit, 1000) {

            public void onTick(long millisUntilFinished) {
                countDownTextView.setTextColor(Color.parseColor("#cc3333"));
                countDownTextView.setText(String.valueOf(millisUntilFinished/1000));
            }

            public void onFinish() {
                if(isRecording){
                    stopRecording();
                }
            }
        };

        countDownTimer3 =  new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                if(!finishRecording) {
                    showRecordingPrompt();
                } else {
                    Intent mainMenuIntent = new Intent(getApplicationContext(), CardStyleMainMenuActivity.class);
                    startActivity(mainMenuIntent);
                    finish();
                }
            }
        };

        countDownTimer4 =  new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                showRecordingPrompt();
            }
        };
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

//    private void initGoogleDrive(){
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(Drive.API)
//                .addScope(Drive.SCOPE_FILE)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .build();
//    }

    private void initUI(){
        int imageResource1 = getResources().getIdentifier("ic_action_accept", "drawable", getPackageName());
        successDrawable = getResources().getDrawable(imageResource1);
        int imageResource2 = getResources().getIdentifier("ic_help_50", "drawable", getPackageName());
        infoDrawable = getResources().getDrawable(imageResource2);
        int imageResource3 = getResources().getIdentifier("ic_timer", "drawable", getPackageName());
        timerDrawable = getResources().getDrawable(imageResource3);
        cameraPreviewFrameLayout = (FrameLayout) findViewById(R.id.camera_preview_framelayout);
        prepContentTextView = (TextView) findViewById(R.id.prep_content_textview);
        prepTitleTextView = (TextView) findViewById(R.id.prep_title_textview);
        recordingPromptTextView = (TextView) findViewById(R.id.recording_prompt_textview);
        testIconImageView = (ImageView) findViewById(R.id.test_icon_imageview);
        testIconImageView.setImageDrawable(infoDrawable);
        testIconImageView.setMinimumWidth(100);
        testIconImageView.setMinimumHeight(100);
        FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params1.gravity = Gravity.CENTER | Gravity.TOP;
        countDownTextView = new TextView(this);
        countDownTextView.setLayoutParams(params1);
        countDownTextView.setTextColor(Color.parseColor("#FFFFFF"));
        countDownTextView.setTextSize(30f);
        countDownTextView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        countDownTextView.setText("");
        countDownTextView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_rec, 0, 0, 0);
    }

    private File getFile(String fileName){
        File file = new File(context.getFilesDir(), fileName);
        return file;
    }

    private void showRecordingPrompt(){
        //testIconImageView.setImageDrawable(timerDrawable);
        prepContentTextView.setVisibility(View.INVISIBLE);
        prepTitleTextView.setVisibility(View.INVISIBLE);
        recordingPromptTextView.setText("You have " + String.valueOf(testTimeLimit/1000) + " seconds");
        countDownTimer1.start();
    }

    private void showPostRecordingPrompts(){
        playSuccessSound();
        testIconImageView.setImageDrawable(successDrawable);
        testIconImageView.setVisibility(View.VISIBLE);
        recordingPromptTextView.setVisibility(View.INVISIBLE);
        prepTitleTextView.setText("Test Complete!");
        //prepContentTextView.setText("Say \"OK Glass, go forward\" to submit");
        //prepContentTextView.setTextColor(Color.parseColor("#16b902"));
        prepTitleTextView.setVisibility(View.VISIBLE);
        //prepContentTextView.setVisibility(View.VISIBLE);
    }

    private void showFinishUploadingVideoPrompts(){
        playLoadingAnimation(false);
        testIconImageView.setImageDrawable(successDrawable);
        testIconImageView.setVisibility(View.VISIBLE);
        prepContentTextView.setTextColor(Color.parseColor("#ffffff"));
        prepTitleTextView.setText("Test submitted! ");
        prepContentTextView.setText("Great Job!");
        countDownTimer3.start();
    }

    private void showUploadingVideoPrompts() {
        playLoadingAnimation(true);
        testIconImageView.setVisibility(View.VISIBLE);
        prepTitleTextView.setVisibility(View.VISIBLE);
        prepContentTextView.setVisibility(View.VISIBLE);
        prepTitleTextView.setText("Submitting");
        prepContentTextView.setTextColor(Color.parseColor("#ffffff"));
        prepContentTextView.setText("Please wait...");
    }

    private void showNoInternetPrompts(){
        playDisallowedSound();
        prepTitleTextView.setText("No Internet Connection");
        prepContentTextView.setTextColor(Color.parseColor("#ffffff"));
        prepContentTextView.setText("Please reconnect and try again");
        countDownTimer3.start();
    }

    private void submitTestResult() {
        Log.i(TAG, "Submitting test result");
        new VideoUpload().execute("");
        //String testReportContent = testReport.generateTestReportContent();
        //saveFileToDrive("report", testReportContent);
        //saveFileToDrive("video", videoFilePath);
    }

    //Check for an Internet connection
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    //Google Drive API

    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Drive connected.");

    }

    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CREATOR:
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Image successfully saved.");
                }
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    private void playLoadingAnimation(boolean play){
        AnimationDrawable uploadAnimation;
        if(play){
            testIconImageView.setImageDrawable(null);
            testIconImageView.setBackgroundResource(R.drawable.upload_anim);
            uploadAnimation = (AnimationDrawable) testIconImageView.getBackground();
            uploadAnimation.start();
        } else {
            testIconImageView.setBackground(null);
        }
    }

    private class VideoUpload extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute(){
            showUploadingVideoPrompts();
        }

        @Override
        protected String doInBackground(String... params) {
            String videoFilePath = getFile(videoOutputFile).toString();
            try {
                mailSender.sendMail(currentLocationString + ". " + videoOutputFile, "", gmailUsername, videoFilePath, videoOutputFile, uploadEmail);
            } catch (Exception e) {
                Log.d(TAG, "Error sending email to upload video to Drive");
            }
            return "Sent";
        }

        @Override
        protected void onPostExecute(String result) {
            new ReportUpload().execute("");
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private class ReportUpload extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute(){
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String reportFilePath = getFile(reportOutputFile).toString();
                String reportBody = testReport.generateTestReportContent();
                FileWriter writer = new FileWriter(getFile(reportOutputFile));
                writer.append(reportBody);
                writer.flush();
                writer.close();
                mailSender.sendMail(currentLocationString + ". " + reportOutputFile, "", gmailUsername, reportFilePath, reportOutputFile, uploadEmail);
            } catch (Exception e) {
                Log.d(TAG, "Error sending email to upload video to Drive");
            }
            return "Sent";
        }

        @Override
        protected void onPostExecute(String result) {
            playSuccessSound();
            showFinishUploadingVideoPrompts();
            hasSubmittedTest = true;
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    /**
     * Create a new file and save it to Drive.
     */
    private void saveFileToDrive(String fileType, String fileContent) {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
        if(fileType == "report"){

        } else if(fileType == "video"){

        }
//        final Bitmap image = mBitmapToSave;
//        Drive.DriveApi.newDriveContents(mGoogleApiClient)
//                .setResultCallback(new ResultCallback<DriveContentsResult>() {
//
//                    @Override
//                    public void onResult(DriveContentsResult result) {
//                        // If the operation was not successful, we cannot do anything
//                        // and must
//                        // fail.
//                        if (!result.getStatus().isSuccess()) {
//                            Log.i(TAG, "Failed to create new contents.");
//                            return;
//                        }
//                        // Otherwise, we can write our data to the new contents.
//                        Log.i(TAG, "New contents created.");
//                        // Get an output stream for the contents.
//                        OutputStream outputStream = result.getDriveContents().getOutputStream();
//                        // Write the bitmap data from it.
//                        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
//                        image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
//                        try {
//                            outputStream.write(bitmapStream.toByteArray());
//                        } catch (IOException e1) {
//                            Log.i(TAG, "Unable to write file contents.");
//                        }
//                        // Create the initial metadata - MIME type and title.
//                        // Note that the user will be able to change the title later.
//                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
//                                .setMimeType("image/jpeg").setTitle("Android Photo.png").build();
//                        // Create an intent for the file chooser, and start it.
//                        IntentSender intentSender = Drive.DriveApi
//                                .newCreateFileActivityBuilder()
//                                .setInitialMetadata(metadataChangeSet)
//                                .setInitialDriveContents(result.getDriveContents())
//                                .build(mGoogleApiClient);
//                        try {
//                            startIntentSenderForResult(
//                                    intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
//                        } catch (IntentSender.SendIntentException e) {
//                            Log.i(TAG, "Failed to launch file chooser.");
//                        }
//                    }
//        });
    }

    private void getLocation() {
        //Get location from WiFi
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            List<String> providers = locationManager.getProviders(true);

            Location currentLocation = null;

            for (int i=providers.size()-1; i>=0; i--) {
                currentLocation = locationManager.getLastKnownLocation(providers.get(i));
                if (currentLocation != null) break;
            }

            if (currentLocation != null) {
                Double currentLatitude = currentLocation.getLatitude();
                Double currentLongitude = currentLocation.getLongitude();
                Geocoder gcd = new Geocoder(context, Locale.getDefault());
                try {
                    List<Address> addresses = gcd.getFromLocation(currentLatitude, currentLongitude, 1);
                    if (addresses.size() > 0) {
                        Log.d(TAG, "Current Address: " + addresses.get(0).getLocality());
                        Log.d(TAG, "Current Address: " + addresses);
                        currentLocationString = addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea();
                    }
                } catch (IOException exception) {
                    Log.d(TAG, "Error parsing address: " + exception);
                }
            }
        }
    }
}