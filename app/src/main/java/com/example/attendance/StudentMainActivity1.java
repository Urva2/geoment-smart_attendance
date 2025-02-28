package com.example.attendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class StudentMainActivity1 extends AppCompatActivity {

    private Spinner spinnerYear, spinnerBranch;
    private EditText etName, etPrn;
    private Button btnSubmit;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

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

        // Initialize Firebase Firestore and Auth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Handle Submit Button
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String year = spinnerYear.getSelectedItem().toString();
                String branch = spinnerBranch.getSelectedItem().toString();
                String name = etName.getText().toString().trim();
                String prn = etPrn.getText().toString().trim();
// After user enters PRN and submits
                String enteredPRN = etPrn.getText().toString().trim();

                if (!enteredPRN.isEmpty()) {
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                   // editor.putString("userPRN", enteredPRN);
                    editor.apply();

                    Toast.makeText(StudentMainActivity1.this, "SharedPreferences updated", Toast.LENGTH_SHORT).show();
                    //Log.d("DEBUG", "Stored PRN: " + enteredPRN);
                } else {
                    Toast.makeText(StudentMainActivity1.this, "Please enter a valid PRN", Toast.LENGTH_SHORT).show();
                }

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(prn)) {
                    Toast.makeText(StudentMainActivity1.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get the currently logged-in user ID
                String userId = auth.getCurrentUser().getUid();

                // Create Data Map for studentdetails
                Map<String, Object> studentData = new HashMap<>();
                studentData.put("name", name);
                studentData.put("prn", prn);
                studentData.put("year", year);
                studentData.put("branch", branch);

                // Save data in Firestore under studentdetails
                db.collection("studentdetails") // Parent collection
                        .document(branch) // Branch (e.g., "IT")
                        .collection(year) // Year (e.g., "FY")
                        .document(prn) // PRN as the document ID
                        .set(studentData) // Set the student data
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Successfully saved in studentdetails, now update users collection
                                db.collection("users").document(userId)
                                        .update("prn", prn) // Add PRN to the existing user document
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(StudentMainActivity1.this, "Data saved successfully!", Toast.LENGTH_SHORT).show();
                                                etName.setText("");
                                                etPrn.setText("");

                                                // Navigate to next activity
                                                Intent intent = new Intent(StudentMainActivity1.this, StudentMainActivity.class);
                                                startActivity(intent);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(StudentMainActivity1.this, "Error updating PRN: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
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
