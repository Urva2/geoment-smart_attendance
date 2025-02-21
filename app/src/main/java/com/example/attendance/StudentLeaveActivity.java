package com.example.attendance;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StudentLeaveActivity extends AppCompatActivity {
    private EditText etStartDate, etEndDate, etReason;
    private Button btnUploadPDF, btnSubmitLeave;
    private Uri pdfUri;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_leave);

        // Initialize UI elements
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etReason = findViewById(R.id.etReason);
        btnUploadPDF = findViewById(R.id.btnUploadPDF);
        btnSubmitLeave = findViewById(R.id.btnSubmitLeave);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        btnUploadPDF.setOnClickListener(v -> selectPDF());
        btnSubmitLeave.setOnClickListener(v -> submitLeaveRequest());
    }

    private void selectPDF() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            pdfUri = data.getData();
            Toast.makeText(this, "PDF Selected Successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitLeaveRequest() {
        if (pdfUri == null || etStartDate.getText().toString().isEmpty() ||
                etEndDate.getText().toString().isEmpty() || etReason.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill all fields and upload PDF", Toast.LENGTH_SHORT).show();
            return;
        }

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userID = auth.getCurrentUser().getUid();  // Get logged-in user ID
        String requestID = UUID.randomUUID().toString(); // Unique ID for request
        StorageReference fileRef = storageRef.child("leave_certificates/" + userID + "/" + requestID + ".pdf");

        // Upload PDF to Firebase Storage
        fileRef.putFile(pdfUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            Log.d("FirebaseStorage", "File uploaded: " + uri.toString());
                            saveLeaveRequestToFirestore(userID, requestID, uri.toString());
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirebaseStorage", "Failed to get URL", e);
                            Toast.makeText(this, "Failed to retrieve file URL", Toast.LENGTH_LONG).show();
                        })
                )
                .addOnFailureListener(e -> {
                    Log.e("FirebaseStorage", "Upload failed", e);
                    Toast.makeText(this, "PDF Upload Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveLeaveRequestToFirestore(String userID, String requestID, String fileUrl) {
        Map<String, Object> leaveData = new HashMap<>();
        leaveData.put("studentName", "John Doe");
        leaveData.put("PRN", "123456");
        leaveData.put("branch", "IT");
        leaveData.put("year", "FY");
        leaveData.put("startDate", etStartDate.getText().toString());
        leaveData.put("endDate", etEndDate.getText().toString());
        leaveData.put("reason", etReason.getText().toString());
        leaveData.put("medicalCertificateUrl", fileUrl);
        leaveData.put("status", "Pending");
        leaveData.put("facultyRemarks", "Not provided");

        // Save to Firestore under "LeaveDetails"
        db.collection("LeaveDetails").document(userID)
                .collection("requests").document(requestID).set(leaveData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Leave Request Sent!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Firestore Save Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreError", "Save failed", e);
                });
    }
}
