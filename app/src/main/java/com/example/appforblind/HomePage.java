package com.example.appforblind;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import androidx.annotation.NonNull;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.net.Uri;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;



import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import static com.google.android.gms.location.LocationServices.*;

public class HomePage extends AppCompatActivity implements
        RecognitionListener {

    private ImageView launch_button;
    public static final int code = 111;

    //private LocationIdentification locationIdentifier;
    //private VoiceRecognition voiceRecognizer;


    // location + Voice .............

    LocationProvider location;
    private FusedLocationProviderClient fusedLocationClient;
    private static SpeechRecognizer speechRecognizer, destinationRecognizer;
    private TextToSpeech textToSpeech;
    private Intent intent;
    private final int REQUEST_LOCATION_PERMISSION = 1;

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private TextView returnedText;
    private TextView returnedError;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";

  //  private TextToSpeech textToSpeech;

    private void resetSpeechRecognizer() {
        textToSpeech.speak("কিভাবে হেল্প করবো", TextToSpeech.QUEUE_FLUSH, null, null);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(speech != null)
            speech.destroy();
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        Log.i(LOG_TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this));


        if(SpeechRecognizer.isRecognitionAvailable(this)) {
            speech.setRecognitionListener(this);

        }
        else {

            finish();
        }
    }

    private void setRecogniserIntent() {
        //textToSpeech.speak("what is my name", TextToSpeech.QUEUE_FLUSH, null, null);

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // map navigator location.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Intent phoneCall = new Intent( getApplicationContext(), PhoneCall.class);

        // UI initialisation
        returnedText = findViewById(R.id.textView6);
        returnedError = findViewById(R.id.textView4);
        progressBar =  findViewById(R.id.progressBar1);
        progressBar.setVisibility(View.INVISIBLE);


        ///////..................
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

            }
        });
        /////////............

        // start speech recogniser
        resetSpeechRecognizer();

        // start progress bar
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);

        // check for permission
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }

        setRecogniserIntent();
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                speech.startListening(recognizerIntent);
            } else {
                Toast.makeText(HomePage.this, "Permission Denied!", Toast
                        .LENGTH_SHORT).show();
                finish();
            }
        }
    }


    @Override
    public void onResume() {
        Log.i(LOG_TAG, "resume");
        super.onResume();
        resetSpeechRecognizer();
        speech.startListening(recognizerIntent);
    }

    @Override
    protected void onPause() {
        Log.i(LOG_TAG, "pause");
        super.onPause();
        speech.stopListening();
    }

    @Override
    protected void onStop() {
        Log.i(LOG_TAG, "stop");
        super.onStop();
        if (speech != null) {
            speech.destroy();
        }
    }


    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");

        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        progressBar.setIndeterminate(true);
        speech.stopListening();
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        /*
        for (String result : matches)
            text += result + "\n";
            */
        String stringval = matches.get(0);
        String voice = "";
        String roadValue= "";
        Boolean searchRoad = false;
        Boolean searchAgain = false;

        // map navigation........................................................
        //.....................................................................
        if(matches!= null) {

            searchAgain = true;
            stringval = stringval.toLowerCase();
            voice = stringval;
            int indexOfRoad = stringval.indexOf("rasta");
            int obstacleFound =  stringval.indexOf("obstacle");
            stringval = (stringval+" ").split(" ")[0];
            if(indexOfRoad != -1) {
                searchRoad = true;
            }
            else  searchRoad = false;

            Log.d("input###########",stringval);

            if(searchRoad == true) {

                searchRoad = false;
                if(stringval.charAt(stringval.length()-1) == 'r') {
                    StringBuffer stringBuffer= new StringBuffer(stringval);
                    stringBuffer.deleteCharAt(stringBuffer.length()-1);
                    stringval = stringBuffer.toString();

                }
                if(stringval.charAt(stringval.length()-1) == 'e') {
                    StringBuffer stringBuffer= new StringBuffer(stringval);
                    stringBuffer.deleteCharAt(stringBuffer.length()-1);
                    stringval = stringBuffer.toString();

                }
                Log.d("Concate ###########",stringval);
                roadValue = stringval;

                String voiceOutput = "আপনাকে " + roadValue + " এর রাস্তা নেভিগেট করা হচ্ছে";
                textToSpeech.speak(voiceOutput, TextToSpeech.QUEUE_FLUSH, null, null);

                if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.M) {

                    if(getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        String finalRoadValue = roadValue;
                        fusedLocationClient.getLastLocation()
                                .addOnSuccessListener(new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        if (location!=null) {
                                            Double lat = location.getLatitude();
                                            Double longt = location.getLongitude();
                                            String value  = "google.navigation:q=" + finalRoadValue;
                                            Uri ggmmIntentUri = Uri.parse(value);
                                            Uri gmmIntentUri = Uri.parse(value);
                                            intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                            intent.setPackage("com.google.android.apps.maps");
                                            startActivity(intent);
                                            //intent.setData(Uri.parse(value));
                                            Log.d("location.", String.valueOf(lat));

                                        }

                                    }
                                });



                    } else{
                        Log.d("location.", "my name is awan");
                        requestPermissions(new  String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                        //stringtext = null;
                    }
                    // requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},validateRequestPermissionsRequestCode(););
                }

            }
            else if( obstacleFound !=-1) {
                returnedText.setText(matches.get(0));
                //startActivity(new Intent(HomePage.this,CameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));

                Log.d("object-detection#####", "start object detection activity");
                Intent intent = new Intent(HomePage.this, CameraActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                finish();
                startActivity(intent);





            }

        }

        returnedText.setText(matches.get(0));
         speech.startListening(recognizerIntent);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.i(LOG_TAG, "FAILED " + errorMessage);
        returnedError.setText(errorMessage);

        // rest voice recogniser
        resetSpeechRecognizer();
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        //Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
    }

    public String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
}