package com.example.attendance;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;

public class splashscreen extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("UserPrefs",MODE_PRIVATE);
        new Handler().postDelayed(this::checkLoginStatus, 2000);
    }
    private void checkLoginStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String role = sharedPreferences.getString("role", null);

        Intent intent;

        if (currentUser != null && isLoggedIn && role != null) {
            // Redirect based on user role
            if ("Student".equals(role)) {
                intent = new Intent(splashscreen.this, StudentMainActivity.class);
            } else {
                intent = new Intent(splashscreen.this, TeacherMainActivity.class);
            }
        } else {
            // If not logged in, go to MainActivity (which leads to login/register)
            intent = new Intent(splashscreen.this, MainActivity.class);
        }

        startActivity(intent);
        finish(); // Close splash screen
}}