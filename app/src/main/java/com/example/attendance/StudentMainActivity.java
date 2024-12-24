package com.example.attendance;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class StudentMainActivity extends AppCompatActivity {

    private Spinner spinnerYear, spinnerBranch;
    private EditText etName, etPrn;
    private Button btnSubmit, btnStudentBookLecture;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);

        // Initialize Views
        spinnerYear = findViewById(R.id.spinner_year);
        spinnerBranch = findViewById(R.id.spinner_branch);
        etName = findViewById(R.id.et_name);
        etPrn = findViewById(R.id.et_prn);
        btnSubmit = findViewById(R.id.btn_submit);
        btnStudentBookLecture = findViewById(R.id.btn_student_book_lecture);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Handle Submit Button
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String year = spinnerYear.getSelectedItem().toString();
                String branch = spinnerBranch.getSelectedItem().toString();
                String name = etName.getText().toString().trim();
                String prn = etPrn.getText().toString().trim();

                if (name.isEmpty() || prn.isEmpty()) {
                    Toast.makeText(StudentMainActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Firestore Path: student_details/branch/year
                String collectionPath = "student_details/" + branch + "/" + year;

                // Create Data Map
                Map<String, Object> studentData = new HashMap<>();
                studentData.put("name", name);
                studentData.put("prn", prn);


                db.collection("studentdetails")
                        .document("N235i5q6WTVbmtMATfeo").collection(branch).document(branch).collection(year).document(year)
                        .set(studentData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(StudentMainActivity.this, "Data saved successfully!", Toast.LENGTH_SHORT).show();
                                etName.setText("");
                                etPrn.setText("");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(StudentMainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Handle Student Book Lecture Button
        btnStudentBookLecture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(StudentMainActivity.this, "Feature under development", Toast.LENGTH_SHORT).show();
                // Add intent here for the next activity when implemented
            }
        });
    }
}