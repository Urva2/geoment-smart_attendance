package com.example.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class StudentMainActivity2 extends AppCompatActivity {

    private EditText prnEditText;
    private Button bookLectureButton;
    private TextView detailsTextView;
    private Button nextStepButton;
    private EditText cmpid;

    private String fetchedId; // Variable to store the fetched ID from the database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main2);

        // Initialize UI elements
        prnEditText = findViewById(R.id.prnEditText);
        bookLectureButton = findViewById(R.id.bookLectureButton);
        detailsTextView = findViewById(R.id.detailsTextView);
        nextStepButton = findViewById(R.id.nextStepButton);
        cmpid = findViewById(R.id.cmpid);

        nextStepButton.setEnabled(false); // Initially disable the Next Step button

        // Set onClickListener for the Book Lecture button
        bookLectureButton.setOnClickListener(v -> {
            String prn = prnEditText.getText().toString().trim();

            if (TextUtils.isEmpty(prn)) {
                Toast.makeText(StudentMainActivity2.this, "Please enter PRN", Toast.LENGTH_SHORT).show();
                return;
            }

            // Fetch the Year and Branch from the student details collection
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("studentdetails")
                    .whereEqualTo("prn", prn)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (querySnapshot.isEmpty()) {
                            Toast.makeText(StudentMainActivity2.this, "No student found with this PRN", Toast.LENGTH_SHORT).show();
                        } else {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String year = document.getString("year");
                                String branch = document.getString("branch");

                                // Use these values to fetch appointment details
                                fetchAppointmentDetails(year, branch);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(StudentMainActivity2.this, "Error fetching student details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // Set onClickListener for the Next Step button
        nextStepButton.setOnClickListener(v -> {
            String enteredId = cmpid.getText().toString().trim();

            if (TextUtils.isEmpty(enteredId)) {
                Toast.makeText(StudentMainActivity2.this, "Please enter your ID", Toast.LENGTH_SHORT).show();
                return;
            }

            if (enteredId.equals(fetchedId)) {
                Toast.makeText(StudentMainActivity2.this, "ID Matched! Proceeding...", Toast.LENGTH_SHORT).show();
                // Navigate to the next activity
                Intent intent = new Intent(StudentMainActivity2.this, AttendanceBookingActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(StudentMainActivity2.this, "ID does not match!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to fetch appointment details based on year and branch
    private void fetchAppointmentDetails(String year, String branch) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("appointmentdetails")
                .whereEqualTo("year", year)
                .whereEqualTo("branch", branch)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        detailsTextView.setText("No appointments found for this year and branch.");
                    } else {
                        StringBuilder details = new StringBuilder();

                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String date = document.getString("date");
                            String time = document.getString("time");
                            String name = document.getString("name");
                            fetchedId = document.getString("id"); // Store the fetched ID

                            // Append each appointment detail to the StringBuilder
                            details.append("Date: ").append(date).append("\n")
                                    .append("Time: ").append(time).append("\n")
                                    .append("Name: ").append(name).append("\n")
                                    .append("ID: ").append(fetchedId).append("\n\n");
                        }

                        // Display the appointment details in the TextView
                        detailsTextView.setText(details.toString());
                        cmpid.setVisibility(View.VISIBLE); // Show the ID input field
                        nextStepButton.setEnabled(true); // Enable the Next Step button for ID comparison
                    }
                })
                .addOnFailureListener(e -> {
                    detailsTextView.setText("Error fetching appointment details: " + e.getMessage());
                });
    }
}