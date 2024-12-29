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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AttendanceBookingActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final double ATTENDANCE_LATITUDE = 22.3233625; // Replace with your predefined latitude
    private static final double ATTENDANCE_LONGITUDE = 73.1794455; // Replace with your predefined longitude
    private static final double RADIUS_IN_METERS =30; // Restrict to 25 meters
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Button bookAttendanceButton;
    private CheckBox bookAttendanceCheckBox;
    private FirebaseFirestore firestore;
    private String prn, branch, year, id, sub;

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

        Toast.makeText(this, "PRN: " + prn + ", Branch: " + branch + ", Year: " + year + ", ID: " + id + ", SUB:" + sub, Toast.LENGTH_LONG).show();

        // Initialize views
        bookAttendanceButton = findViewById(R.id.bookAttendanceButton);
        bookAttendanceCheckBox = findViewById(R.id.bookAttendanceCheckBox);

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }

        // Set up live location updates
        setupLocationCallback();

        // Set onClick listener for the Book Attendance button
        bookAttendanceButton.setOnClickListener(view -> {
            if (bookAttendanceCheckBox.isChecked()) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Attendance booking not selected.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied. Cannot access location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                // Get the latest location
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // Calculate the distance between the current location and the attendance location
                    float[] distance = new float[1];
                    Location.distanceBetween(latitude, longitude, ATTENDANCE_LATITUDE, ATTENDANCE_LONGITUDE, distance);

                    // Check if the distance is within the 25-meter radius
                    if (distance[0] <= RADIUS_IN_METERS) {
                        markAttendance("P");
                        Toast.makeText(AttendanceBookingActivity.this, "Attendance booked successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        markAttendance("A");
                        Toast.makeText(AttendanceBookingActivity.this, "You are outside the attendance area. Attendance not booked.", Toast.LENGTH_SHORT).show();
                    }

                    // Stop location updates after attendance is booked
                    stopLocationUpdates();

                    // Redirect after 2 seconds
                    new Handler().postDelayed(() -> {
                        Intent intent = new Intent(AttendanceBookingActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }, 2000);
                }
            }
        };
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000); // Check location every second

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void markAttendance(String status) {
        firestore = FirebaseFirestore.getInstance();
        Map<String, Object> lectureData = new HashMap<>();
        lectureData.put("attend", status);
        lectureData.put("prn", prn);

        firestore.collection("attendancedetails")
                .document(branch)
                .collection(year)
                .document(sub)
                .collection(id)
                .document(prn)
                .set(lectureData)
                .addOnSuccessListener(documentReference -> {
                    // Success message can be added here if needed
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to book attendance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
           });
}
}