package com.example.attendance;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class TeacherMainActivity extends AppCompatActivity {

    private Button btnAppointLecture, btnViewPastAttendance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_main);

        // Initialize buttons
        btnAppointLecture = findViewById(R.id.btnAppointLecture);
        btnViewPastAttendance = findViewById(R.id.btnViewPastAttendance);

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
    }
}