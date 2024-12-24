package com.example.attendance;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AppointLectureActivity extends AppCompatActivity {

    private EditText editTextDate, editTextTime, editTextLectureName, editTextID;
    private Button btnAppoint;

    // Firestore instance
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appoint_lecture);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize views
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);
        editTextLectureName = findViewById(R.id.editTextLectureName);
        editTextID = findViewById(R.id.editTextID);
        btnAppoint = findViewById(R.id.btnAppoint);

        // Set button click listener
        btnAppoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get data from input fields
                String date = editTextDate.getText().toString().trim();
                String time = editTextTime.getText().toString().trim();
                String lectureName = editTextLectureName.getText().toString().trim();
                String id = editTextID.getText().toString().trim();

                // Validate inputs
                if (TextUtils.isEmpty(date) || TextUtils.isEmpty(time) ||
                        TextUtils.isEmpty(lectureName) || TextUtils.isEmpty(id)) {
                    Toast.makeText(AppointLectureActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Store data in Firestore
                Map<String, Object> lectureData = new HashMap<>();
                lectureData.put("date", date);
                lectureData.put("time", time);
                lectureData.put("namelec", lectureName);
                lectureData.put("id", id);

                firestore.collection("appointmentdetails")
                        .add(lectureData)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(AppointLectureActivity.this, "Lecture appointed successfully!", Toast.LENGTH_SHORT).show();
                            clearFields();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AppointLectureActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    // Clear input fields after successful data entry
    private void clearFields() {
        editTextDate.setText("");
        editTextTime.setText("");
        editTextLectureName.setText("");
        editTextID.setText("");
    }
}