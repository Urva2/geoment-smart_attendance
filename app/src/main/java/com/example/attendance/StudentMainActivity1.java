package com.example.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class StudentMainActivity1 extends AppCompatActivity {

    private Spinner spinnerYear, spinnerBranch;
    private EditText etName, etPrn;
    private Button btnSubmit;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main1);

        // Initialize Views
        spinnerYear = findViewById(R.id.spinner_year);
        spinnerBranch = findViewById(R.id.spinner_branch);
        etName = findViewById(R.id.et_name);
        etPrn = findViewById(R.id.et_prn);
        btnSubmit = findViewById(R.id.btn_submit);

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

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(prn)) {
                    Toast.makeText(StudentMainActivity1.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create Data Map
                Map<String, Object> studentData = new HashMap<>();
                studentData.put("name", name);
                studentData.put("prn", prn);
                studentData.put("year", year);
                studentData.put("branch", branch);

                // Save data in Firestore
                db.collection("studentdetails") // Parent collection
                        .document(branch) // Branch (e.g., "IT")
                        .collection(year) // Year (e.g., "FY")
                        .document(prn) // PRN as the document ID
                        .set(studentData) // Set the student data
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(StudentMainActivity1.this, "Data saved successfully!", Toast.LENGTH_SHORT).show();
                                etName.setText("");
                                etPrn.setText("");

                                // You can navigate to another activity if needed
                                Intent intent=new Intent(StudentMainActivity1.this,StudentMainActivity2.class);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(StudentMainActivity1.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}