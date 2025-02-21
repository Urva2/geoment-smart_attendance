package com.example.attendance;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.io.IOException;
public class splashscreen extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SurfaceView surfaceView = findViewById(R.id.splashSurfaceView);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                playVideo(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                // No action needed
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            }
        });
    }

    private void playVideo(SurfaceHolder holder) {
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash2); // Ensure the video is in res/raw
        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(this, videoUri);
            mediaPlayer.setDisplay(holder);
            mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
            mediaPlayer.setOnCompletionListener(mp -> checkLoginStatus());
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                checkLoginStatus();
                return true;
            });

            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            checkLoginStatus(); // Handle errors by proceeding
        }
    }

    private void checkLoginStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String role = sharedPreferences.getString("role", null);

        Intent intent;

        if (currentUser != null && isLoggedIn && role != null) {
            if ("Student".equals(role)) {
                intent = new Intent(splashscreen.this, StudentMainActivity.class);
            } else {
                intent = new Intent(splashscreen.this, TeacherMainActivity.class);
            }
        } else {
            intent = new Intent(splashscreen.this, MainActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
