package com.example.attendance;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
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

    // Fingerprint manager
    private FingerprintManager fingerprintManager;
    private FingerprintManager.CryptoObject cryptoObject;

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

        // Initialize FingerprintManager
        fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        // Initially hide the Book Attendance button
        bookAttendanceButton.setVisibility(Button.INVISIBLE);

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

        // Checkbox change listener
        bookAttendanceCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Show fingerprint authentication prompt
                authenticateFingerprint();
            } else {
                // Hide the button if the checkbox is unchecked
                bookAttendanceButton.setVisibility(Button.INVISIBLE);
            }
        });
    }

    private void authenticateFingerprint() {
        // Check if fingerprint authentication is available
        if (fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints()) {
            // Create a fingerprint authentication callback
            FingerprintManager.AuthenticationCallback fingerprintAuthCallback = new FingerprintManager.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    // On successful fingerprint authentication, show the "Book Attendance" button
                    bookAttendanceButton.setVisibility(Button.VISIBLE);
                    Toast.makeText(AttendanceBookingActivity.this, "Fingerprint authentication successful", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(AttendanceBookingActivity.this, "Fingerprint authentication failed", Toast.LENGTH_SHORT).show();
                }
            };

            // Start the fingerprint authentication process
            fingerprintManager.authenticate(cryptoObject, null, 0, fingerprintAuthCallback, null);
        } else {
            Toast.makeText(this, "Fingerprint authentication is not available or no fingerprints enrolled.", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle the permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get location
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
                            lectureData.put("attend", distance[0] <= radius ? "P" : "A");
                            lectureData.put("prn", prn);

                            // Save attendance in Firestore with proper path
                            firestore.collection("attendancedetails")
                                    .document(branch)
                                    .collection(year)
                                    .document(sub)
                                    .collection(id )
                                    .document(prn)// Assuming students is a sub-collection
                                    // Save attendance with student's PRN
                                    .set(lectureData)
                                    .addOnSuccessListener(documentReference -> {
                                        // Optional: handle success
                                        String message = distance[0] <= radius ? "Attendance booked successfully!" : "You are outside the attendance area. Attendance not booked.";
                                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle failure
                                        Toast.makeText(this, "Error saving attendance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });

                            // Redirect after saving attendance
                            new Handler().postDelayed(() -> {
                                Intent intent = new Intent(AttendanceBookingActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }, 2000);
                        }
                    });
        } else {
            // Permission is not granted, so request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }
}