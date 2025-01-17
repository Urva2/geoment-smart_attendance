package com.example.attendance;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class splashscreen extends AppCompatActivity {

    private SurfaceView surfaceView;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        surfaceView = findViewById(R.id.surfaceView);

        // Set up SurfaceHolder for the SurfaceView
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                playVideo(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                // Handle surface changes if necessary
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            }
        });
    }

    private void playVideo(SurfaceHolder holder) {
        // Initialize the MediaPlayer
        mediaPlayer = new MediaPlayer();
        try {
            // Replace with your video file path or URL
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.now;
            mediaPlayer.setDataSource(this, android.net.Uri.parse(videoPath));
            mediaPlayer.setDisplay(holder);
            mediaPlayer.prepareAsync();

            // Start video playback when ready
            mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}