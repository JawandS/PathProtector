package com.singhjawand.pathprotector;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Dictionary;

public class MainActivity extends Activity implements GPSCallback {
    private GPSManager gpsManager = null;
    Boolean isGPSEnabled = false;
    LocationManager locationManager;
    double currentSpeed = 0.0;
    double maxSpeed = 0.0;
    TextView currentSpeedTxt;
    TextView maxSpeedTxt;
    TextView statusTxt;
    double drivingThreshold = 5.0;
    double movingThreshold = 0.3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // set up
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get views
        currentSpeedTxt = (TextView) findViewById(R.id.currentSpeed);
        maxSpeedTxt = (TextView) findViewById(R.id.maxSpeed);
        statusTxt = (TextView) findViewById(R.id.status);

        // access resources
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // start tracking speed
        getCurrentSpeed();
    }

    public void getCurrentSpeed() {
        currentSpeedTxt.setText(getString(R.string.info));
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        gpsManager = new GPSManager(MainActivity.this);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGPSEnabled) {
            gpsManager.startListening(getApplicationContext());
            gpsManager.setGPSCallback(this);
        } else {
            gpsManager.showSettingsAlert();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onGPSUpdate(Location location) {
        currentSpeed = location.getSpeed();
        currentSpeed = round(currentSpeed, 3, BigDecimal.ROUND_HALF_UP);
        currentSpeedTxt.setText("Current speed: " + currentSpeed + " m/s");

        if (currentSpeed > maxSpeed) { // update maximum speed
            maxSpeed = currentSpeed;
            maxSpeedTxt.setText("Max speed: " + currentSpeed + " m/s");
        }

        // updates status
        if (currentSpeed > drivingThreshold) { // car
            statusTxt.setText("Status: Driving");
        } else if (currentSpeed > movingThreshold) { // walking
            statusTxt.setText("Status: Moving");
        } else { // still
            statusTxt.setText("Status: Still");
        }
    }

    @Override
    protected void onDestroy() {
        gpsManager.stopListening();
        gpsManager.setGPSCallback(null);
        gpsManager = null;
        super.onDestroy();
    }

    public static double round(double unrounded, int precision, int roundingMode) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        return rounded.doubleValue();
    }
}