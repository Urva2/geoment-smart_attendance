package com.example.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StudentMainActivity2 extends AppCompatActivity {

    // Views
    private EditText prnEditText, subjectNameEditText, subjectIdEditText;
    private TextView detailsTextView;
    private Button bookLectureButton, fetchLectureDetailsButton, nextStepButton;

    // Firestore
    private FirebaseFirestore db;

    // Global variables for the entire package
    public static String branch = "";
    public static String year = "";
    public static String subjectName = "";
    public static String subjectID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main2);

        // Initialize views
        prnEditText = findViewById(R.id.prnEditText);
        subjectNameEditText = findViewById(R.id.editText2);
        subjectIdEditText = findViewById(R.id.editText3);
        detailsTextView = findViewById(R.id.detailsTextView);
        bookLectureButton = findViewById(R.id.bookLectureButton);
        fetchLectureDetailsButton = findViewById(R.id.button2);
        nextStepButton = findViewById(R.id.nextStepButton);

        // Hide unnecessary fields initially
        subjectNameEditText.setVisibility(View.GONE);
        subjectIdEditText.setVisibility(View.GONE);
        fetchLectureDetailsButton.setVisibility(View.GONE);


        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Button listeners
        bookLectureButton.setOnClickListener(v -> {
            String prn = prnEditText.getText().toString().trim();
            if (!prn.isEmpty()) {
                fetchStudentDetails(prn);
            } else {
                Toast.makeText(this, "Please enter PRN", Toast.LENGTH_SHORT).show();
            }
        });

        fetchLectureDetailsButton.setOnClickListener(v -> {
            // Get values from EditTexts and assign them to global variables
            subjectName = subjectNameEditText.getText().toString().trim();
            subjectID = subjectIdEditText.getText().toString().trim();

            if (subjectName.isEmpty() || subjectID.isEmpty()) {
                Toast.makeText(this, "Please enter subject name and ID", Toast.LENGTH_SHORT).show();
            } else {
                fetchAppointmentDetails(); // Proceed to fetch lecture details
            }
        });

        nextStepButton.setOnClickListener(v -> {
            Intent intent = new Intent(StudentMainActivity2.this, AttendanceBookingActivity.class);
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
                                                // Fetch and store branch and year
                                                branch = studentDoc.getString("branch");
                                                year = studentDoc.getString("year");

                                                Toast.makeText(
                                                        StudentMainActivity2.this,
                                                        "Found student in " + year + " of " + branch,
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                // Show additional fields
                                                subjectNameEditText.setVisibility(View.VISIBLE);
                                                subjectIdEditText.setVisibility(View.VISIBLE);
                                                fetchLectureDetailsButton.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("FirestoreError", "Error fetching year: " + e.getMessage()));
                        }
                    }

                    new Handler().postDelayed(() -> {
                        if (!prnFound[0]) {
                            detailsTextView.setText("PRN not found in any branch or year.");
                        }
                    }, 2000);
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Error fetching branches: " + e.getMessage()));
    }

    private void fetchAppointmentDetails() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Use global variables: branch, year, subjectName, subjectID
        db.collection("appointmentdetails")
                .document(branch)
                .collection(year)
                .document(subjectName)
                .collection(subjectID)
                .get() // Fetch all documents in this collection
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        StringBuilder appointmentDetails = new StringBuilder();

                        // Loop through all documents in the query result
                        for (QueryDocumentSnapshot appointmentDoc : querySnapshot) {
                            // Check if the document matches today's date and subjectID
                            String fetchedDate = appointmentDoc.getString("date");
                            String fetchedId = appointmentDoc.getString("id");

                            if (today.equals(fetchedDate) && subjectID.equals(fetchedId)) {
                                String subject = appointmentDoc.getString("namelec");
                                String date = appointmentDoc.getString("date");
                                String time = appointmentDoc.getString("time");
                                String id = appointmentDoc.getString("id");

                                // Append details to the StringBuilder
                                appointmentDetails.append("Subject: ").append(subject)
                                        .append("\nDate: ").append(date)
                                        .append("\nTime: ").append(time)
                                        .append("\nLecture ID: ").append(id).append("\n\n");
                            }
                        }

                        if (appointmentDetails.length() > 0) {
                            // Display the details in your TextView
                            detailsTextView.setText(appointmentDetails.toString());
                        } else {
                            // No matching appointments found
                            detailsTextView.setText("No matching appointments found for today.");
                        }
                    } else {
                        // Handle case where no documents are found
                        detailsTextView.setText("No appointments found for the specified path.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error fetching appointments: " + e.getMessage());
                    detailsTextView.setText("Error fetching appointments. Please try again.");
                });
    }

}