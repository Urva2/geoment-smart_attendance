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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AttendanceBookingActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private Button bookAttendanceButton;
    private CheckBox bookAttendanceCheckBox;
    private FirebaseFirestore firestore;
    private String prn, branch, year, id, sub;

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

        // Request location permissions if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize FingerprintManager
        fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        // Initially hide the Book Attendance button
        bookAttendanceButton.setVisibility(Button.INVISIBLE);

        // Set onClick listener for the Book Attendance button
        bookAttendanceButton.setOnClickListener(view -> {
            if (bookAttendanceCheckBox.isChecked()) {
                // Get location and book attendance
                getLocationAndBookAttendance();
            } else {
                Toast.makeText(this, "Attendance booking not selected.", Toast.LENGTH_SHORT).show();
            }
        });

        // Checkbox change listener
        bookAttendanceCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Authenticate via fingerprint
                authenticateFingerprint();
            } else {
                bookAttendanceButton.setVisibility(Button.INVISIBLE);
            }
        });
    }

    private void authenticateFingerprint() {
        if (fingerprintManager != null && fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints()) {
            FingerprintManager.AuthenticationCallback fingerprintAuthCallback = new FingerprintManager.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    bookAttendanceButton.setVisibility(Button.VISIBLE);
                    Toast.makeText(AttendanceBookingActivity.this, "Fingerprint authentication successful", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(AttendanceBookingActivity.this, "Fingerprint authentication failed", Toast.LENGTH_SHORT).show();
                }
            };

            fingerprintManager.authenticate(cryptoObject, null, 0, fingerprintAuthCallback, null);
        } else {
            Toast.makeText(this, "Fingerprint authentication not available.", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle permission request results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied. Cannot access location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Fetch accurate location and book attendance
    private void getLocationAndBookAttendance() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Request a fresh location with high accuracy
            fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            // Display fetched latitude & longitude
                            Toast.makeText(this, "Lat = " + latitude + ", Lng = " + longitude, Toast.LENGTH_LONG).show();

                            // Predefined attendance location
                            double attendanceLatitude = 22.3237635;
                            double attendanceLongitude = 73.1781624;
                            double radius = 100;  // In meters

                            // Calculate distance
                            float[] distance = new float[1];
                            Location.distanceBetween(latitude, longitude, attendanceLatitude, attendanceLongitude, distance);

                            // Save attendance status
                            Map<String, Object> lectureData = new HashMap<>();
                            lectureData.put("attend", distance[0] <= radius ? "P" : "A");
                            lectureData.put("prn", prn);

                            // Save in Firestore
                            firestore.collection("attendancedetails")
                                    .document(branch)
                                    .collection(year)
                                    .document(sub)
                                    .collection(id)
                                    .document(prn)
                                    .set(lectureData)
                                    .addOnSuccessListener(documentReference -> {
                                        String message = distance[0] <= radius ? "Attendance booked successfully!" : "You are outside the attendance area.";
                                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error saving attendance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });

                            // Redirect after 2 seconds
                            new Handler().postDelayed(() -> {
                                Intent intent = new Intent(AttendanceBookingActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }, 2000);
                        } else {
                            Toast.makeText(this, "Could not get accurate location. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Location request failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }
}