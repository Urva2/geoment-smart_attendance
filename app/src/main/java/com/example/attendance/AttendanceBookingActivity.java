package com.example.attendance;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ktx.Firebase;

import java.util.HashMap;
import java.util.Map;

public class AttendanceBookingActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private Button bookAttendanceButton;
    private CheckBox bookAttendanceCheckBox;
    private FirebaseFirestore firestore;
    String prn,branch,year,id,sub;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_booking);
        // Retrieve the passed data
        prn = getIntent().getStringExtra("prn");
        branch = getIntent().getStringExtra("branch");
        year = getIntent().getStringExtra("year");
        id = getIntent().getStringExtra("id");
        sub= getIntent().getStringExtra("sub");
        // Use the values as needed
        Toast.makeText(this, "PRN: " + prn + ", Branch: " + branch + ", Year: " + year + ", ID: " + id + ", SUB:" + sub, Toast.LENGTH_LONG).show();



        // Initialize views
        bookAttendanceButton = findViewById(R.id.bookAttendanceButton);
        bookAttendanceCheckBox = findViewById(R.id.bookAttendanceCheckBox);

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }

        // Set onClick listener for the Book Attendance button
        bookAttendanceButton.setOnClickListener(view -> {
            // Check if the user wants to book attendance
            if (bookAttendanceCheckBox.isChecked()) {
                // Get location and check if within radius for attendance booking
                getLocationAndBookAttendance();
            } else {
                Toast.makeText(this, "Attendance booking not selected.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Handle the permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get location
                //getLocationAndBookAttendance();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied. Cannot access location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to get the user's current location and check if they are within the attendance radius
    private void getLocationAndBookAttendance() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // Once we have the location, check if it's within the predetermined radius
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            // For example, let's assume the predetermined location is at this latitude and longitude
                            double attendanceLatitude =22.3235482;  // Replace with your predefined latitude
                            double attendanceLongitude =  73.1795980;  // Replace with your predefined longitude
                            double radius = 100;  // In meters

                            // Calculate the distance between the current location and the attendance location
                            float[] distance = new float[1];
                            Location.distanceBetween(latitude, longitude, attendanceLatitude, attendanceLongitude, distance);

                            // Check if the distance is less than or equal to the radius
                            if (distance[0] <= radius) {

                                //inserting data into database if above condition is true and entering p into attend field of firestore database
                              //  Intent intent1=getIntent();
                                // String getprn = intent1.getStringExtra("prn");

                                firestore = FirebaseFirestore.getInstance();
                                Map<String, Object> lectureData = new HashMap<>();
                                lectureData.put("attend","P");
                                lectureData.put("prn",prn);

                                firestore.collection("attendancedetails")
                                        .document(branch)  // This is a specific document under "appointmentdetails"// Document for the selected year
                                        .collection(year)
                                        .document(sub)
                                        .collection(id)
                                        .document(id)
                                        .set(lectureData)// Store the lecture data in that specific path
                                        .addOnSuccessListener(documentReference -> {

                                        })
                                        .addOnFailureListener(e -> {
                                        });
                            }

                                // Attendance is within the radius
                                Toast.makeText(this, "Attendance booked successfully!", Toast.LENGTH_SHORT).show();

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(AttendanceBookingActivity.this,LoginActivity.class);
                                        startActivity(intent);

                                    }
                                },2000);

                                // Here you can save the attendance to the Firestore database or perform any other action
                                // For example, call a method to store attendance in Firestore
                                // saveAttendanceToFirestore(userId, groupId, subject);
                            } else {
                            firestore = FirebaseFirestore.getInstance();
                            Map<String, Object> lectureData = new HashMap<>();
                            lectureData.put("attend","A");
                            lectureData.put("prn",prn);

                            firestore.collection("attendancedetails")
                                    .document(branch)  // This is a specific document under "appointmentdetails"// Document for the selected year
                                    .collection(year)
                                    .document(sub)
                                    .collection(id)
                                    .document(prn)
                                    .set(lectureData)// Store the lecture data in that specific path
                                    .addOnSuccessListener(documentReference -> {

                                    })
                                    .addOnFailureListener(e -> {
                                    });
                                Toast.makeText(this, "You are outside the attendance area. Attendance not booked.", Toast.LENGTH_SHORT).show();


                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(AttendanceBookingActivity.this,LoginActivity.class);
                                        startActivity(intent);

                                    }
                                },2000);
                            }

                    });

        } else {
            // Permission is not granted, so request the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
}
    }
}
