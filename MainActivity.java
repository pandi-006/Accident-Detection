package com.example.accidentdetection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import android.telephony.SmsManager;
import android.content.Intent;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button buttonEmergency, buttonSendSMS, button1;
    TextView textView, textView1;
    double latitude, longitude;

    SensorManager sensorManager;
    Sensor accelerometer;
    float lastX, lastY, lastZ;
    long lastTime;
    static final int SHAKE_THRESHOLD = 800;

    FusedLocationProviderClient fusedLocationClient;
    CountDownTimer countDownTimer;
    boolean isAlertCancelled = false;
    DBHelper dbHelper;

    private static final int SMS_PERMISSION_CODE = 101;
    private static final int LOCATION_PERMISSION_CODE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonEmergency = findViewById(R.id.button3);
       // buttonSendSMS = findViewById(R.id.buttonSendSMS);
        button1 = findViewById(R.id.button1);
        textView = findViewById(R.id.textView);
        textView1 = findViewById(R.id.textView1);

        dbHelper = new DBHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        getLocation(); // get location immediately

        // Manual emergency details
        buttonEmergency.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EmergencyDetailsActivity.class);
            startActivity(intent);
        });

        button1.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Profile.class);
            startActivity(intent);
        });

        // Test SMS
       // buttonSendSMS.setOnClickListener(v -> sendTestSMS());

//        buttonSendSMS.setOnClickListener(v -> {
        //   checkEmergencyContacts(); // just show numbers in Toast
//        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorListener);
    }

    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastTime) > 100) {
                long diffTime = currentTime - lastTime;
                lastTime = currentTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    startEmergencyCountdown();
                }

                lastX = x;
                lastY = y;
                lastZ = z;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    // ------------------- LOCATION -------------------
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                textView.setText("Latitude : " + latitude);
                textView1.setText("Longitude : " + longitude);
            }
        });
    }

    // ------------------- COUNTDOWN -------------------
    private void startEmergencyCountdown() {
        isAlertCancelled = false;

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Emergency Alert")
                .setMessage("Sending alert in 10 seconds...")
                .setCancelable(false)
                .setNegativeButton("CANCEL", (d, w) -> {
                    isAlertCancelled = true;
                    if (countDownTimer != null) countDownTimer.cancel();
                })
                .show();

        countDownTimer = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                dialog.setMessage("Sending alert in " + (millisUntilFinished / 1000) + " seconds...");
            }

            public void onFinish() {
                dialog.dismiss();
                if (!isAlertCancelled) {
                    sendEmergencySMSAutomatically();
                }
            }
        }.start();
    }

    // ------------------- SEND EMERGENCY SMS AUTOMATICALLY -------------------
    private void sendEmergencySMSAutomatically() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
            return;
        }

        Cursor cursor = dbHelper.getAllEmergencyContacts();
        if (cursor != null && cursor.moveToFirst()) {
            SmsManager smsManager = SmsManager.getDefault();

            do {
                String mobile = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_MOBILE));

                if (mobile != null && !mobile.isEmpty()) {

                    String mapLink = "https://maps.google.com/?q=" + latitude + "," + longitude;
                   // String mapLink = latitude + "," + longitude;

                    //String message = "Alert: ";
                    String message = "Alert: " + mapLink;
                    try {
                        smsManager.sendTextMessage(mobile, null, message, null, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to send SMS to " + mobile, Toast.LENGTH_SHORT).show();
                    }
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        Toast.makeText(this, "Emergency SMS sent to all contacts!", Toast.LENGTH_LONG).show();
    }

    // ------------------- SEND TEST SMS -------------------
    private void sendTestSMS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
            return;
        }

        try {
            String receiver = "+919597968191";
            String message = "Test SMS from my app!";
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(receiver, null, message, null, null);
            Toast.makeText(this, "Test SMS sent successfully!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to send SMS!", Toast.LENGTH_SHORT).show();
        }
    }

    // ------------------- PERMISSIONS CALLBACK -------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (granted) {
                // If test button was clicked, send test SMS
                // If shake triggered, send emergency SMS automatically
                sendEmergencySMSAutomatically();
            } else {
                Toast.makeText(this, "SMS permission denied!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOCATION_PERMISSION_CODE) {
            getLocation();
        }
    }

    private void checkEmergencyContacts() {
        Cursor cursor = dbHelper.getAllEmergencyContacts();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String mobile = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_MOBILE));
                Toast.makeText(this, "Contact: " + mobile, Toast.LENGTH_SHORT).show();
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Toast.makeText(this, "No contacts found in database", Toast.LENGTH_SHORT).show();
        }
    }


}
