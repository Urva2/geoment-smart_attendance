package com.example.attendance;
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
import com.google.firebase.firestore.QuerySnapshot;

public class StudentMainActivity2 extends AppCompatActivity {

    private EditText prnEditText;
    private Button bookLectureButton;
    private TextView detailsTextView;
    private Button nextStepButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main2);

        // Initialize UI elements
        prnEditText = findViewById(R.id.prnEditText);
        bookLectureButton = findViewById(R.id.bookLectureButton);
        detailsTextView = findViewById(R.id.detailsTextView);
        nextStepButton = findViewById(R.id.nextStepButton);

        // Set onClickListener for the Book Lecture button
        bookLectureButton.setOnClickListener(v -> {
            String prn = prnEditText.getText().toString().trim();

            if (TextUtils.isEmpty(prn)) {
                Toast.makeText(StudentMainActivity2.this, "Please enter PRN", Toast.LENGTH_SHORT).show();
                return;
            }

            // Fetch the Year and Branch from the student details collection group
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("studentdetails")  // Using collectionGroup to access all student details
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

        // Set onClickListener for Next Step button
        nextStepButton.setOnClickListener(v -> {
            Toast.makeText(StudentMainActivity2.this, "This feature is under development", Toast.LENGTH_SHORT).show();
        });
    }

    // Method to fetch appointment details based on year and branch
    private void fetchAppointmentDetails(String year, String branch) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Using collectionGroup to access all appointment details collections
        db.collection("appointmentdetails")  // Using collectionGroup for appointment details
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
                            String id = document.getString("id");

                            // Append each appointment detail to the StringBuilder
                            details.append("Date: ").append(date).append("\n")
                                    .append("Time: ").append(time).append("\n")
                                    .append("Name: ").append(name).append("\n")
                                    .append("ID: ").append(id).append("\n\n");
                        }

                        // Display the appointment details in the TextView
                        detailsTextView.setText(details.toString());
                    }
                })
                .addOnFailureListener(e -> {
                    detailsTextView.setText("Error fetching appointment details: " + e.getMessage());
                });
    }
}