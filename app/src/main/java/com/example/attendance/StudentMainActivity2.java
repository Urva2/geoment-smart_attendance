package com.example.attendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;




public class StudentMainActivity2 extends AppCompatActivity {

    private EditText prnEditText, subjectNameEditText, subjectIdEditText;
    private TextView detailsTextView;
    private Button bookLectureButton, fetchLectureDetailsButton, nextStepButton;
    private FirebaseFirestore db;
    public static String branch = "";
    public static String year = "";
    public static String subjectName = "";
    public static String subjectID = "";
    private String userPRN = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main2);

        // Initialize Views
        prnEditText = findViewById(R.id.prnEditText);
        subjectNameEditText = findViewById(R.id.editText2);
        subjectIdEditText = findViewById(R.id.editText3);
        detailsTextView = findViewById(R.id.detailsTextView);
        bookLectureButton = findViewById(R.id.fetchPRNButton);
        fetchLectureDetailsButton = findViewById(R.id.button2);
        nextStepButton = findViewById(R.id.nextStepButton);

        subjectNameEditText.setVisibility(View.GONE);
        subjectIdEditText.setVisibility(View.GONE);
        fetchLectureDetailsButton.setVisibility(View.GONE);

        db = FirebaseFirestore.getInstance();

        // Retrieve PRN from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userPRN = sharedPreferences.getString("userPRN", "");

        // Auto-fill PRN and disable editing
        prnEditText.setText(userPRN);
        prnEditText.setEnabled(false);

        bookLectureButton.setOnClickListener(v -> {
            if (!userPRN.isEmpty()) {
                fetchStudentDetails(userPRN);
            } else {
                Toast.makeText(this, "PRN not found. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        fetchLectureDetailsButton.setOnClickListener(v -> {
            subjectName = subjectNameEditText.getText().toString().trim();
            subjectID = subjectIdEditText.getText().toString().trim();
            if (subjectName.isEmpty() || subjectID.isEmpty()) {
                Toast.makeText(this, "Please enter subject name and ID", Toast.LENGTH_SHORT).show();
            } else {
                fetchAppointmentDetails();
            }
        });

        nextStepButton.setOnClickListener(v -> {
            Intent intent = new Intent(StudentMainActivity2.this, AttendanceBookingActivity.class);
            intent.putExtra("prn", userPRN);
            intent.putExtra("branch", branch);
            intent.putExtra("year", year);
            intent.putExtra("id", subjectID);
            intent.putExtra("sub", subjectName);
            startActivity(intent);
        });
    }

    private void fetchStudentDetails(String prn) {
        db.collection("studentdetails")
                .get()
                .addOnSuccessListener(branchSnapshots -> {
                    boolean[] prnFound = {false};
                    for (QueryDocumentSnapshot branchDoc : branchSnapshots) {
                        String branchName = branchDoc.getId();
                        for (String yearOption : getResources().getStringArray(R.array.year_options)) {
                            db.collection("studentdetails").document(branchName)
                                    .collection(yearOption)
                                    .whereEqualTo("prn", prn)
                                    .get()
                                    .addOnSuccessListener(yearSnapshots -> {
                                        if (!yearSnapshots.isEmpty() && !prnFound[0]) {
                                            prnFound[0] = true;
                                            for (QueryDocumentSnapshot studentDoc : yearSnapshots) {
                                                branch = studentDoc.getString("branch");
                                                year = studentDoc.getString("year");
                                                Toast.makeText(this, "Found student in " + year + " of " + branch, Toast.LENGTH_SHORT).show();
                                                subjectNameEditText.setVisibility(View.VISIBLE);
                                                subjectIdEditText.setVisibility(View.VISIBLE);
                                                fetchLectureDetailsButton.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Error fetching branches: " + e.getMessage()));
    }

    private void fetchAppointmentDetails() {
        db.collection("lectures")
                .whereEqualTo("subjectName", subjectName)
                .whereEqualTo("subjectID", subjectID)
                .whereEqualTo("branch", branch)
                .whereEqualTo("year", year)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        StringBuilder lectureDetails = new StringBuilder();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String lectureDate = document.getString("date");
                            String lectureTime = document.getString("time");
                            lectureDetails.append("Date: ").append(lectureDate).append("\nTime: ").append(lectureTime).append("\n\n");
                        }
                        detailsTextView.setText(lectureDetails.toString());
                    } else {
                        detailsTextView.setText("No lectures found for the provided details.");
                    }
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Error fetching lecture details: " + e.getMessage()));
    }
}
