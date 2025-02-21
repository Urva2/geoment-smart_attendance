package com.example.attendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class TeacherMainActivity extends AppCompatActivity {

    private Button btnAppointLecture, btnViewPastAttendance, chat;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_main);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // Force Light Mode
        // Initialize buttons
        btnAppointLecture = findViewById(R.id.btnAppointLecture);
        btnViewPastAttendance = findViewById(R.id.btnViewPastAttendance);
        chat = findViewById(R.id.chat);
        // Logout Button
        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> logoutUser());
        // Set click listeners for buttons
        btnAppointLecture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to AppointLectureActivity
                Intent intent = new Intent(TeacherMainActivity.this, AppointLectureActivity.class);
                startActivity(intent);
            }
        });

        btnViewPastAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ViewAttendanceActivity
                Intent intent = new Intent(TeacherMainActivity.this, ViewAttendanceActivity.class);
                startActivity(intent);
            }
        });

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Teacher Chat Activity (Chat functionality)
                Intent intent = new Intent(TeacherMainActivity.this, teacherchat.class);
                startActivity(intent);
            }
        });
    }
    private void logoutUser() {
        // Sign out from Firebase
        mAuth.signOut();

        // Clear login state in SharedPreferences
        sharedPreferences.edit()
                .putBoolean("isLoggedIn", false)
                .remove("role")
                .apply();

        // Redirect to MainActivity
        Intent intent = new Intent(TeacherMainActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears backstack
        startActivity(intent);
        finish(); // Close current activity
    }
}