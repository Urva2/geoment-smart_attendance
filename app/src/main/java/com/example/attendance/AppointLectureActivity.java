package com.example.attendance;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AppointLectureActivity extends AppCompatActivity {

    private EditText editTextDate, editTextTime, editTextLectureName, editTextID;
    private Spinner spinnerBranch, spinnerYear;
    private Button btnAppoint;

    // Firestore instance
    private FirebaseFirestore firestore;

    // Array for branch and year (assumed predefined)

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
        spinnerBranch = findViewById(R.id.spinnerBranch);
        spinnerYear = findViewById(R.id.spinnerYear);
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
                String branch = spinnerBranch.getSelectedItem().toString().trim();
                String year = spinnerYear.getSelectedItem().toString().trim();

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
                lectureData.put("branch", branch);
                lectureData.put("year", year);


                // Store the lecture data in the Firestore path
                firestore.collection("appointmentdetails")
                        .document("appointmentdetails")  // This is a specific document under "appointmentdetails"// Document for the selected year
                        .set(lectureData)                // Store the lecture data in that specific path
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(AppointLectureActivity.this, "Lecture appointed successfully!", Toast.LENGTH_SHORT).show();
                            clearFields();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AppointLectureActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            // Clear input fields after successful data entry
            private void clearFields() {
                editTextDate.setText("");
                editTextTime.setText("");
                editTextLectureName.setText("");
                editTextID.setText("");
                spinnerBranch.setSelection(0);
                spinnerYear.setSelection(0);
            }
        });
    }
}