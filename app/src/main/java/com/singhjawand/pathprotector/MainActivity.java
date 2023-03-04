package com.singhjawand.pathprotector;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    TextView mainText;
    SensorManager sensorManager;
    Sensor sensor; // general sensor
    Sensor accelerometer;
    double ax, ay, az;   // acceleration in x, y, z

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainText = (TextView) findViewById(R.id.sampleText);
        mainText.setText("got it!");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer
            mainText.setText("have accel");
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL * 100);
        } else {
            // fail! we dont have an accelerometer!
            mainText.setText("no accel :(");
        }
    }

    private final SensorEventListener listener = new SensorEventListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onSensorChanged(SensorEvent event) {
            // The acceleration may be negative, so take their absolute value
            float xValue = Math.abs(event.values[0]);
            float yValue = Math.abs(event.values[1]);
            float zValue = Math.abs(event.values[2]);
            mainText.setText(xValue + " " + yValue + " " + zValue);
            if (xValue > 10 || yValue > 10 || zValue > 10) {
                // message for user
                mainText.setText(xValue + " " + yValue + " " + zValue);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
}