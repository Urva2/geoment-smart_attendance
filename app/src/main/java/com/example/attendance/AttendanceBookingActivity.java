package com.example.attendance;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AttendanceBookingActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private Button bookAttendanceButton;
    private CheckBox bookAttendanceCheckBox;
    private FirebaseFirestore firestore;
    String prn, branch, year, id, sub;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_booking);

        // Retrieve the passed data
        prn = getIntent().getStringExtra("prn");
        branch = getIntent().getStringExtra("branch");
        year = getIntent().getStringExtra("year");
        id = getIntent().getStringExtra("id");
        sub = getIntent().getStringExtra("sub");

        // Use the values as needed
        Toast.makeText(this, "PRN: " + prn + ", Branch: " + branch + ", Year: " + year + ", ID: " + id + ", SUB: " + sub, Toast.LENGTH_LONG).show();

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
                getLocationAndBookAttendance();
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
                            double attendanceLatitude = 22.2917628;  // Replace with your predefined latitude
                            double attendanceLongitude = 73.1994581;  // Replace with your predefined longitude
                            double radius = 100;  // In meters

                            // Calculate the distance between the current location and the attendance location
                            float[] distance = new float[1];
                            Location.distanceBetween(latitude, longitude, attendanceLatitude, attendanceLongitude, distance);

                            // Check if the distance is less than or equal to the radius
                            firestore = FirebaseFirestore.getInstance();
                            Map<String, Object> lectureData = new HashMap<>();

                            if (distance[0] <= radius) {
                                // If present, mark as "P"
                                lectureData.put("attend", "P");
                            } else {
                                // If not present, mark as "E"
                                lectureData.put("attend", "A");
                            }

                            // Add student details to the Firestore
                            lectureData.put("prn", prn);

                            firestore.collection("attendancedetails")
                                    .document(branch)  // This is a specific document under "attendance details"
                                    .collection(year)   // Document for the selected year
                                    .document(sub)      // Subject code
                                    .collection(id)     // Collection for the subject ID
                                    .document(prn)      // Document for the student's PRN
                                    .set(lectureData)    // Store the lecture data in that specific path
                                    .addOnSuccessListener(documentReference -> {
                                        // Attendance successfully recorded
                                        Toast.makeText(this, "Attendance recorded successfully!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle failure
                                        Toast.makeText(this, "Error recording attendance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });

                            // Navigate to the login activity after a brief delay
                            new Handler().postDelayed(() -> {
                                Intent intent = new Intent(AttendanceBookingActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }, 2000);

                        } else {
                            Toast.makeText(this, "Unable to get location. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Permission is not granted, so request the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }
}