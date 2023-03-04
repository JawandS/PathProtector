package com.singhjawand.pathprotector;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.math.BigDecimal;

public class MainActivity extends Activity implements GPSCallback {
    private GPSManager gpsManager = null;
    Boolean isGPSEnabled = false;
    LocationManager locationManager;
    double currentSpeed = 0.0;
    double maxSpeed = 0.0;

    // text views
    TextView currentSpeedTxt;
    TextView maxSpeedTxt;
    TextView statusTxt;
    TextView otherInfoTxt;

    double drivingThreshold = 0.4; // default is 2.7 m/s
    //    double movingThreshold = 0.3;
    double firstTs = 0;
    double timestamp;
    int timestampCounter = 0;
    double lastPause = 0;

    // driving information
    boolean isDriving = false;
    double maxWaitTime = 5 * 1000; // default is 4 minutes --> milliseconds
    double minDriveTime = 1000; // default is 1 minute --> milliseconds

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // set up
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firstTs = System.currentTimeMillis();

        // get views
        currentSpeedTxt = findViewById(R.id.currentSpeed);
        maxSpeedTxt = findViewById(R.id.maxSpeed);
        statusTxt = findViewById(R.id.status);
        otherInfoTxt = findViewById(R.id.otherInfo);

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
        currentSpeed = round(currentSpeed, 3);
        currentSpeedTxt.setText("Current speed: " + currentSpeed + " m/s");

        if (currentSpeed > maxSpeed) { // update maximum speed
            maxSpeed = currentSpeed;
            maxSpeedTxt.setText("Max speed: " + currentSpeed + " m/s");
        }

        // updates status
        if (currentSpeed > drivingThreshold) { // car
            timestamp = System.currentTimeMillis();
            timestampCounter += 1;
            statusTxt.setText("Status: Driving");
            if (!isDriving) { // began driving
                firstTs = timestamp;
                isDriving = true;
            }
        } else if (isDriving && timestamp - lastPause > maxWaitTime) { // done driving
            isDriving = false;
            if (timestamp - firstTs > minDriveTime) // check drive is long enough
                promptUser(); // ask the user whether to store drive
        } else {
            statusTxt.setText("Status: Still");
        }
    }

    void promptUser() {
        Log.v("ImportantInfo", "Saving data");
        // length of trip
        otherInfoTxt.setText(String.valueOf(round((timestamp - firstTs) / 1000, 3)));
    }

    @Override
    protected void onDestroy() {
        gpsManager.stopListening();
        gpsManager.setGPSCallback(null);
        gpsManager = null;
        super.onDestroy();
    }

    public static double round(double unrounded, int precision) {
        int roundingMode = BigDecimal.ROUND_HALF_UP;
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        return rounded.doubleValue();
    }
}

/*statusTxt.setText("Update frequency: " + String.valueOf(round((timestamp - firstTs) / 1000 / timestampCounter, 3, BigDecimal.ROUND_HALF_UP)));
        final String TAG = "important info";
        Log.v(TAG, "Critical: " + (timestamp - firstTs));
        Log.v(TAG, "Critical: " + timestampCounter);
        Log.v(TAG, "Critical: " + (timestamp - firstTs) / 1000 / timestampCounter); */