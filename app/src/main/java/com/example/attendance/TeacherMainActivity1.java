package com.example.attendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class TeacherMainActivity1 extends AppCompatActivity {

    private EditText etName, etFacultyID, etDepartment;
    private Button btnSubmit;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher1_main);

        etName = findViewById(R.id.etName);
        etFacultyID = findViewById(R.id.etFacultyID);
        etDepartment = findViewById(R.id.etDepartment);
        btnSubmit = findViewById(R.id.btnSubmit);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        // Set Input Filter to allow only 5-digit numeric Faculty ID
        etFacultyID.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        // Real-time validation for 5-digit numeric input
        etFacultyID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 5 && !s.toString().matches("\\d{5}")) {
                    etFacultyID.setError("Enter a valid 5-digit number");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTeacherData();
            }
        });
    }

    private void saveTeacherData() {
        String name = etName.getText().toString().trim();
        String facultyID = etFacultyID.getText().toString().trim();
        String department = etDepartment.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            return;
        }
        if (facultyID.isEmpty() || facultyID.length() != 5 || !facultyID.matches("\\d{5}")) {
            etFacultyID.setError("Enter a valid 5-digit Faculty ID");
            return;
        }
        if (department.isEmpty()) {
            etDepartment.setError("Department is required");
            return;
        }
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save data to Firestore
        String userID = user.getUid();
        DocumentReference teacherRef = db.collection("teachers").document(userID);

        Map<String, Object> teacherData = new HashMap<>();
        teacherData.put("name", name);
        teacherData.put("facultyID", facultyID);
        teacherData.put("department", department);

        teacherRef.set(teacherData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Successfully saved in teacherData, now update users collection
                        Map<String, Object> updateData = new HashMap<>();
                        updateData.put("facultyID", facultyID);
                        updateData.put("name", name);
                        updateData.put("department", department);

                        db.collection("users").document(userID)
                                .update(updateData) // Update both fields in Firestore
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(TeacherMainActivity1.this, "Data saved successfully!", Toast.LENGTH_SHORT).show();
                                        etFacultyID.setText("");
                                        etDepartment.setText("");

                                        // Navigate to next activity
                                        Intent intent = new Intent(TeacherMainActivity1.this, TeacherMainActivity.class);
                                        intent.putExtra("teacherName", name); // Pass the name
                                        startActivity(intent);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(TeacherMainActivity1.this, "Error updating user data", Toast.LENGTH_SHORT).show();
                                    }
        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(TeacherMainActivity1.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

              /*  .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TeacherMainActivity1.this, "Profile saved!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(TeacherMainActivity1.this, TeacherMainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(TeacherMainActivity1.this, "Error saving data", Toast.LENGTH_SHORT).show());
}
}*/
