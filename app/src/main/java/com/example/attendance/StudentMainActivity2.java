package com.example.attendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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
        bookLectureButton = findViewById(R.id.fetchPRNButton);
        fetchLectureDetailsButton = findViewById(R.id.button2);
        nextStepButton = findViewById(R.id.nextStepButton);

        // Hide unnecessary fields initially
        subjectNameEditText.setVisibility(View.GONE);
        subjectIdEditText.setVisibility(View.GONE);
        fetchLectureDetailsButton.setVisibility(View.GONE);


        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("StudentInfo", MODE_PRIVATE);
        String prn = sharedPreferences.getString("PRN", "");
        branch =  sharedPreferences.getString("Department", "");
        year =sharedPreferences.getString("Year", "");
        // Auto-fill PRN and disable editing
        if (!prn.isEmpty()) {
            prnEditText.setText(prn);
            prnEditText.setEnabled(false);
        } else {
            Toast.makeText(this, "PRN not found. Please try again.", Toast.LENGTH_SHORT).show();
        }
        bookLectureButton.setOnClickListener(v -> {
            if (!prn.isEmpty()) {
                Toast.makeText(this, "Found student in " + year + " of " + branch, Toast.LENGTH_SHORT).show();
                subjectNameEditText.setVisibility(View.VISIBLE);
                subjectIdEditText.setVisibility(View.VISIBLE);
                fetchLectureDetailsButton.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "PRN not found. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
        // Button listeners
       /* bookLectureButton.setOnClickListener(v -> {
            String prn = prnEditText.getText().toString().trim();
            if (!prn.isEmpty()) {
                fetchStudentDetails(prn);
            } else {
                Toast.makeText(this, "Please enter PRN", Toast.LENGTH_SHORT).show();
            }
        });*/

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
            intent.putExtra("prn", prnEditText.getText().toString().trim());
            intent.putExtra("branch", branch);
            intent.putExtra("year", year);
            intent.putExtra("id", subjectID);
            intent.putExtra("sub", subjectName);
            intent.putExtra("prn", prnEditText.getText().toString().trim());
            intent.putExtra("branch", branch);
            intent.putExtra("year", year);
            intent.putExtra("id", subjectID);
            startActivity(intent);

        });
    }

   /* private void fetchStudentDetails(String prn) {
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
*/

    private void fetchAppointmentDetails() {
        String today = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new Date()); // Use "d/M/yyyy" to match Firestore
        Log.d("DateCheck", "Today's Date: " + today);

        db.collection("appointmentdetails")
                .document(branch)
                .collection(year)
                .document(subjectName)
                .collection(subjectID)
                .document(subjectID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fetchedDate = documentSnapshot.getString("date");
                        Log.d("DateCheck", "Fetched Date: " + fetchedDate);

                        if (fetchedDate != null && areDatesEqual(today, fetchedDate)) {
                            String subject = documentSnapshot.getString("namelec");
                            String time = documentSnapshot.getString("time");
                            String id = documentSnapshot.getString("id");

                            detailsTextView.setText("Subject: " + subject + "\nDate: " + fetchedDate + "\nTime: " + time +
                                    "\nLecture ID: " + id);
                        } else {
                            detailsTextView.setText("No matching appointments found for today.");
                        }
                    } else {
                        detailsTextView.setText("No appointments found for the specified path.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error fetching appointments: " + e.getMessage());
                    detailsTextView.setText("Error fetching appointments. Please try again.");
                });
    }

    /**
     * Compares two dates after converting them to a uniform format.
     */
    private boolean areDatesEqual(String date1, String date2) {
        SimpleDateFormat formatter = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()); // Handles both "5/3/2025" and "05/03/2025"
        try {
            Date parsedDate1 = formatter.parse(date1);
            Date parsedDate2 = formatter.parse(date2);
            return parsedDate1.equals(parsedDate2);
        } catch (ParseException e) {
            Log.e("DateParseError", "Error parsing dates: " + e.getMessage());
            return false;
        }
    }
}