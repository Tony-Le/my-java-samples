package ca.eecsyorku.letony28.scrollcompare;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class MainActivity extends Activity implements SensorEventListener, View.OnClickListener {

    final String MYDEBUG = "MYDEBUG";

    final int PRACTICE_TARGETS[] = {20, 10};
    final int NUMBER_TARGETS[] = {28, 64, 72, 55, 16, 50, 41, 82, 41, 79};
    int currentNumber; // The number currently selected by number picker
    int targetsCompleted;
    long completionTimes[] = new long[NUMBER_TARGETS.length];
    long overshootTimeAmount[] = new long[NUMBER_TARGETS.length];

    boolean targetTimerStarted;
    final int TARGET_WAIT_TIME_MS = 2000;
    final int TARGET_TICK_TIME_MS = 500;
    final int MS_PER_SEC = 1000;

    boolean timerStarted;
    long startTime;
    long endTime;
    boolean overshootTimerStarted;
    long overshootStartTime;
    long overshootEndTime;
    long totalOvershootTime;

    int inputMode;
    boolean practiceMode;
    final int TILT_AND_BUTTON_MODE = 1;
    final int BUTTON_MODE = 2;
    final int TILT_MODE = 3;

    LinearLayout tiltPart, buttonPart;
    NumberPicker numberpicker;
    TextView targetNumber, selectedNumber, tiltStatusTextView, increaseButton, decreaseButton;
    Button tiltButton;
    boolean tiltOn;

    private SensorManager sensorManager;
    private Sensor sensor;
    float initPitch, pitch;
    final int TILT_SENSITIVITY_FACTOR = 5;

    Vibrator vb;


    BufferedWriter sd;
    File f;
    String sdLeader;
    final String APP = "ScrollCompare";
    final String WORKING_DIRECTORY = "/ScrollCompareData/";
    final String SD_HEADER = "App,Participant,Session,Block,Group,Condition,TargetsCompleted,InputMode,CompletionTime (ms),TotalOvershootTime (ms),";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        targetNumber = (TextView) findViewById(R.id.targetNumber);
        numberpicker = (NumberPicker) findViewById(R.id.numberPicker);
        tiltStatusTextView = (TextView) findViewById(R.id.tiltStatus);
        buttonPart = (LinearLayout) findViewById(R.id.buttonPart);
        decreaseButton = (TextView) findViewById(R.id.decreaseButton);
        increaseButton = (TextView) findViewById(R.id.increaseButton);
        tiltButton = (Button) findViewById(R.id.tiltButton);
        tiltPart = (LinearLayout) findViewById(R.id.tiltPart);

        numberpicker.setMinValue(0);
        numberpicker.setMaxValue(100);
        numberpicker.setValue(0);
        //disable keyboard for number picker
        numberpicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        //disable touch scrolling and the hidden buttons on numberpicker
        numberpicker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        numberpicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        Bundle b = getIntent().getExtras();

        String participantCode = b.getString("participantCode");
        String sessionCode = b.getString("sessionCode");
        String groupCode = b.getString("groupCode");
        practiceMode = b.getBoolean("practiceMode");
        inputMode = b.getInt("inputMode");

        targetsCompleted = 0;
        currentNumber = 0;
        targetTimerStarted = false;

        timerStarted = false;

        if (practiceMode) {
            targetNumber.setText("" + PRACTICE_TARGETS[0]);
        }
        else {
            targetNumber.setText("" + NUMBER_TARGETS[0]);
        }

        if (inputMode == BUTTON_MODE) {
            tiltPart.setVisibility(View.GONE);

            decreaseButton.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    numberpicker.setValue(numberpicker.getValue() - 1);
                    currentNumber = numberpicker.getValue();
                    checkTargetNumber();
                }
            }));

            increaseButton.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    numberpicker.setValue(numberpicker.getValue() + 1);
                    currentNumber = numberpicker.getValue();
                    checkTargetNumber();
                }
            }));
        }
        else if (inputMode == TILT_MODE) {
            buttonPart.setVisibility(View.GONE);
            tiltButton.setOnClickListener(this);
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            numberpicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    currentNumber = newVal;
                    checkTargetNumber();
                }
            });
        }
        else {
            decreaseButton.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    numberpicker.setValue(numberpicker.getValue() - 1);
                    currentNumber = numberpicker.getValue();
                    checkTargetNumber();
                }
            }));

            increaseButton.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    numberpicker.setValue(numberpicker.getValue() + 1);
                    currentNumber = numberpicker.getValue();
                    checkTargetNumber();
                }
            }));

            tiltButton.setOnClickListener(this);
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            numberpicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    currentNumber = newVal;
                    checkTargetNumber();
                }
            });
        }

        vb = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        if (!practiceMode) {
            // ===================
            // File initialization
            // ===================

            // make a working directory (if necessary) to store data files
            File dataDirectory = new File(Environment.getExternalStorageDirectory() + WORKING_DIRECTORY);
            if (!dataDirectory.exists() && !dataDirectory.mkdirs()) {
                Log.e(MYDEBUG, "ERROR --> FAILED TO CREATE DIRECTORY: " + WORKING_DIRECTORY);
                super.onDestroy(); // cleanup
                this.finish(); // terminate
            }
            Log.i(MYDEBUG, "Working directory=" + dataDirectory);

            /*
             * The following do-loop creates data files for output and a string sdLeader to write to the sd
             * output files.  Both the filenames and the sdLeader are constructed by combining the setup parameters
             * so that the filenames and sdLeader are unique and also reveal the conditions used for the block of input.
             *
             * The block code begins "B01" and is incremented on each loop iteration until an available
             * filename is found.  The goal, of course, is to ensure data files are not inadvertently overwritten.
             */
            int blockNumber = 0;
            do {
                ++blockNumber;
                String blockCode = String.format(Locale.CANADA, "B%02d", blockNumber);
                String baseFilename = String.format("%s-%s-%s-%s-%s-%s", APP, participantCode,
                        sessionCode, blockCode, groupCode, "C" + inputMode);
                f = new File(dataDirectory, baseFilename + ".sd");

                // also make a comma-delimited leader that will begin each data line written to the sd file
                sdLeader = String.format("%s,%s,%s,%s,%s,%s", APP, participantCode, sessionCode,
                        blockCode, groupCode, "C" + inputMode);
            }
            while (f.exists());

            try {
                sd = new BufferedWriter(new FileWriter(f));

                // output header in sd file
                StringBuilder sd_header_add = new StringBuilder();
                for (int i = 0; i < NUMBER_TARGETS.length; i++) {
                    sd_header_add.append(String.format(Locale.CANADA, "CompletionTime%d (ms),", i));
                }
                for (int i = 0; i < NUMBER_TARGETS.length; i++) {
                    sd_header_add.append(String.format(Locale.CANADA, "OvershootAmount%d (ms),", i));
                }
                sd_header_add.append("\n");
                sd.write(SD_HEADER + sd_header_add.toString(), 0, SD_HEADER.length() + sd_header_add.length());
                sd.flush();
            }
            catch (IOException e) {
                Log.e(MYDEBUG, "ERROR OPENING DATA FILES! e=" + e.toString());
                super.onDestroy();
                this.finish();
            } // end file initialization
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (inputMode != BUTTON_MODE) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (inputMode != BUTTON_MODE) {
            sensorManager.unregisterListener(this);
        }

    }

    public void onClick(View v) {
        if (v.equals(tiltButton)) {
            tiltOn = !tiltOn;
            if (tiltOn) {
                initPitch = pitch;
                tiltStatusTextView.setText("Enabled");
                tiltStatusTextView.setTextColor(Color.GREEN);
            }
            else {
                tiltStatusTextView.setText("Disabled");
                tiltStatusTextView.setTextColor(Color.GRAY);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not needed, but we need to provide an implementation anyway
    }


    @Override
    public void onSensorChanged(SensorEvent se) {
        pitch = se.values[1] * TILT_SENSITIVITY_FACTOR;

        if (tiltOn) {
            int scrollAmount = Math.round(initPitch - pitch);
            numberpicker.scrollBy(0, scrollAmount);
        }

        //Log.i(MYDEBUG, "Roll magnitude: " + (initPitch - pitch));
    }

    private void checkTargetNumber() {
        if (!timerStarted) {
            timerStarted = true;
            startTime = System.currentTimeMillis();
        }
        if (!targetTimerStarted) {
            if (practiceMode) {
                if (currentNumber == PRACTICE_TARGETS[targetsCompleted]) {
                    startOvershootTimer();
                    targetTimerStarted = true;
                    new CountDownTimer(TARGET_WAIT_TIME_MS, TARGET_TICK_TIME_MS) {

                        public void onTick(long millisUntilFinished) {
                            if (currentNumber != PRACTICE_TARGETS[targetsCompleted]) {
                                this.cancel();
                                targetTimerStarted = false;
                            }
                        }

                        public void onFinish() {
                            if (currentNumber == PRACTICE_TARGETS[targetsCompleted]) {
                                vb.vibrate(MS_PER_SEC);
                                nextTarget();
                            }
                            targetTimerStarted = false;
                        }
                    }.start();
                }
            }
            else {
                if (currentNumber == NUMBER_TARGETS[targetsCompleted]) {
//                    Log.i(MYDEBUG, "SAW TARGET");
                    startOvershootTimer();
                    targetTimerStarted = true;
                    new CountDownTimer(TARGET_WAIT_TIME_MS, TARGET_TICK_TIME_MS) {

                        public void onTick(long millisUntilFinished) {
                            if (currentNumber != NUMBER_TARGETS[targetsCompleted]) {
                                this.cancel();
                                targetTimerStarted = false;
                            }
                        }

                        public void onFinish() {
                            if (currentNumber == NUMBER_TARGETS[targetsCompleted]) {
//                                Log.i(MYDEBUG, "GOT TARGET");
                                vb.vibrate(MS_PER_SEC);
                                nextTarget();
                            }
                            else {
                                Log.i(MYDEBUG, "NOT TARGET");
                            }
                            targetTimerStarted = false;
                        }
                    }.start();
                }
            }
        }
    }

    private void nextTarget() {
        logTarget();
        targetsCompleted++;
        if (practiceMode) {
            if (targetsCompleted < PRACTICE_TARGETS.length) {
                targetNumber.setText("" + PRACTICE_TARGETS[targetsCompleted]);
            }
            else {
                endTime = System.currentTimeMillis();
                showResults();
            }
        }
        else {
            if (targetsCompleted < NUMBER_TARGETS.length) {
                targetNumber.setText("" + NUMBER_TARGETS[targetsCompleted]);
            }
            else {
                endTime = System.currentTimeMillis();
                showResults();
            }
        }
    }

    private void logTarget() {
        completionTimes[targetsCompleted] = (System.currentTimeMillis() - startTime);
        finishOvershootTimer();
    }

    private void showResults() {
//        Log.i(MYDEBUG, "completion time" + Arrays.toString(completionTimes));
//        Log.i(MYDEBUG,"overshoot times" +  Arrays.toString(overshootTimeAmount));
        long completionTime = (endTime - startTime);
        long overshootingTime = 0;
        for (int i = 0; i < overshootTimeAmount.length; i++) {
            overshootingTime = overshootingTime + overshootTimeAmount[i];
        }

        if (!practiceMode) {
            StringBuilder sbData = new StringBuilder();
            sbData.append(String.format("%s,", sdLeader));
            sbData.append(String.format(Locale.CANADA, "%d,", targetsCompleted));
            if (inputMode == BUTTON_MODE) {
                sbData.append(String.format(Locale.CANADA, "%s,", "Button"));
            }
            else if (inputMode == TILT_MODE) {
                sbData.append(String.format(Locale.CANADA, "%s,", "Tilt"));
            }
            else {
                sbData.append(String.format(Locale.CANADA, "%s,", "Button and Tilt"));
            }
            sbData.append(String.format(Locale.CANADA, "%d,", completionTime));
            sbData.append(String.format(Locale.CANADA, "%d,", overshootingTime));
            for (int i = 0; i < completionTimes.length; i++) {
                sbData.append(String.format(Locale.CANADA, "%d,", completionTimes[i]));
            }
            for (int i = 0; i < overshootTimeAmount.length; i++) {
                sbData.append(String.format(Locale.CANADA, "%d,", overshootTimeAmount[i]));
            }
            try {
                sd.write(sbData + "\n", 0, sbData.length() + 1);
                sd.flush();
            }
            catch (IOException e) {
                Log.e(MYDEBUG, "ERROR WRITING TO sd TRACE DATA FILE!\n" + e);
                this.finish();
            }
            MediaScannerConnection.scanFile(this, new String[]{f.getAbsolutePath()}, null, null);
        }
        Intent i = new Intent(this, ResultsActivity.class);
        Bundle b = new Bundle();
        b.putInt("targetsCompleted", targetsCompleted);
        if (inputMode == BUTTON_MODE) {
            b.putString("inputMode", "Button");
        }
        else if (inputMode == TILT_MODE) {
            b.putString("inputMode", "Tilt");
        }
        else {
            b.putString("inputMode", "Button and Tilt");
        }
        b.putLong("completionTime", completionTime / MS_PER_SEC);
        b.putLong("overshootingTime", overshootingTime / MS_PER_SEC);
        i.putExtras(b);

        this.startActivity(i);
        this.finish();
    }

    private void startOvershootTimer() {
        if (!overshootTimerStarted) {
            overshootStartTime = System.currentTimeMillis();
            overshootTimerStarted = true;
        }
    }

    private void finishOvershootTimer() {
        if (overshootTimerStarted) {
            overshootEndTime = System.currentTimeMillis() - TARGET_TICK_TIME_MS;
            overshootTimerStarted = false;
            overshootTimeAmount[targetsCompleted] = (overshootEndTime - overshootStartTime);
        }
    }
}