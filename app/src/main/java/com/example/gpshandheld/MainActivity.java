package com.example.gpshandheld;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView longitudeText, latitudeText, pseudorange, logTextView;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        longitudeText = findViewById(R.id.longitudeText);
        latitudeText = findViewById(R.id.latitudeText);
        logTextView = findViewById(R.id.logTextView);
        pseudorange = findViewById(R.id.pseduorange);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable GPS", Toast.LENGTH_LONG).show();
            return;
        }

        requestLocationPermission(this, this);
        startGnssMeasurements();
    }

    private void requestLocationPermission(Context context, Activity activity) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            double val = startGnssMeasurements();
            String stringVal = Double.toString(val);
            pseudorange.setText(stringVal);
        }
    }

    @SuppressLint("MissingPermission")
    private double startGnssMeasurements() {
        double[] pseudorangeList = new double[1];
        Context context1 = this;

        GnssMeasurementsEvent.Callback gnssMeasurementEventCallback = new GnssMeasurementsEvent.Callback() {
            @Override
            public void onGnssMeasurementsReceived(GnssMeasurementsEvent eventArgs) {
//                super.onGnssMeasurementsReceived(eventArgs);

                for (GnssMeasurement measurement : eventArgs.getMeasurements()) {
//                    logGnssData(measurement, context1);
                    //access psedorange
                    if (measurement.getState() == GnssMeasurement.STATE_TOW_DECODED){
                        Log.d("GNSS", "measurement.getState: " + 1);
                        double pseudorange = measurement.getPseudorangeRateMetersPerSecond();
                        Log.d("GNSS", "Pseudorange: " + pseudorange);
                        appendLog("GNSS", "Pseudorange", pseudorange);

//                        int i = pseudorangeList.length;
                        pseudorangeList[0] = pseudorange;
                    }
                }
            }

            @Override
            public void onStatusChanged(int status) {
                super.onStatusChanged(status);
//                onGnssMeasurementsReceived();
                Log.d("GNSS", "Status Changed : " + status);
                Log.d("GNSS", "Pseudorange: " + Arrays.toString(pseudorangeList));
                appendLog("GNSS", "Status Changed", status);
                appendLog("GNSS", "Pseudorange List 0", pseudorangeList[0]);
            }
        };

        //register callback based on event
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { //for API 30+
            Executor executor = Executors.newSingleThreadExecutor();
//            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
            locationManager.registerGnssMeasurementsCallback(executor, gnssMeasurementEventCallback);
        } else {
            locationManager.registerGnssMeasurementsCallback(gnssMeasurementEventCallback);
        }

        return pseudorangeList[0];
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Executor executor = Executors.newSingleThreadExecutor();
            locationManager.unregisterGnssMeasurementsCallback(new GnssMeasurementsEvent.Callback() {
                @Override
                public void onGnssMeasurementsReceived(GnssMeasurementsEvent eventArgs) {
                    super.onGnssMeasurementsReceived(eventArgs);
                }
            });
        } else {
            locationManager.unregisterGnssMeasurementsCallback(new GnssMeasurementsEvent.Callback() {
                @Override
                public void onGnssMeasurementsReceived(GnssMeasurementsEvent eventArgs) {
                    super.onGnssMeasurementsReceived(eventArgs);
                }
            });
        }
    }

    private void logGnssData(GnssMeasurement event, Context context) {
        Toast.makeText(context, "GNSS Data Received", Toast.LENGTH_SHORT).show();
    }

    private void appendLog(String tag, String message, double pseduorange) {
        String preudorangeString = String.valueOf(pseduorange);
        String initString = String.format("Tag : %s, %s : %s", tag, message, preudorangeString);
        logTextView.append(initString + "\n");
    }
}