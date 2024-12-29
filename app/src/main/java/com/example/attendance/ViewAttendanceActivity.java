package com.example.attendance;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewAttendanceActivity extends AppCompatActivity {

    private EditText branchEditText, yearEditText, subjectEditText, subjectIdEditText;
    private Button submitButton;
    private RecyclerView attendanceRecyclerView;
    private FirebaseFirestore db;
    private AttendanceAdapter attendanceAdapter;
    private List<Attendance> attendanceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        branchEditText = findViewById(R.id.branchEditText);
        yearEditText = findViewById(R.id.yearEditText);
        subjectEditText = findViewById(R.id.subjectEditText);
        subjectIdEditText = findViewById(R.id.subjectIdEditText);
        submitButton = findViewById(R.id.submitButton);
        attendanceRecyclerView = findViewById(R.id.attendanceRecyclerView);

        // Initialize RecyclerView
        attendanceList = new ArrayList<>();
        attendanceAdapter = new AttendanceAdapter(attendanceList);
        attendanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        attendanceRecyclerView.setAdapter(attendanceAdapter);

        // Submit Button Click Listener
        submitButton.setOnClickListener(view -> {
            String branch = branchEditText.getText().toString().trim();
            String year = yearEditText.getText().toString().trim();
            String subject = subjectEditText.getText().toString().trim();
            String subjectId = subjectIdEditText.getText().toString().trim();

            if (branch.isEmpty() || year.isEmpty() || subject.isEmpty() || subjectId.isEmpty()) {
                Toast.makeText(ViewAttendanceActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                fetchAttendanceData(branch, year, subject, subjectId);
            }
        });
    }

    private void fetchAttendanceData(String branch, String year, String subject, String subjectId) {
        // Step 1: Fetch all PRNs and Names from studentdetails collection
        db.collection("studentdetails")
                .document(branch)
                .collection(year)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> prns = new ArrayList<>();
                        List<String> names = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            prns.add(document.getId()); // PRN is the document ID
                            names.add(document.getString("name")); // Name field
                        }

                        // Step 2: Fetch attendance data for each PRN from attendancedetails collection
                        fetchAttendanceDetails(prns, names, branch, year, subject, subjectId);
                    } else {
                        Toast.makeText(ViewAttendanceActivity.this, "Error fetching student details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchAttendanceDetails(List<String> prns, List<String> names, String branch, String year, String subject, String subjectId) {
        attendanceList.clear(); // Clear any previous data

        for (int i = 0; i < prns.size(); i++) {
            String prn = prns.get(i);
            String name = names.get(i);

            db.collection("attendancedetails")
                    .document(branch)
                    .collection(year)
                    .document(subject)
                    .collection(subjectId)
                    .document(prn) // Fetch the document for the specific PRN
                    .get()
                    .addOnCompleteListener(task -> {
                        String attendanceStatus = "A"; // Default attendance is "A"
                        if (task.isSuccessful() && task.getResult().exists()) {
                            String attend = task.getResult().getString("attend");
                            if ("P".equals(attend)) {
                                attendanceStatus = "P";
                            }
                        }

                        // Add the data to the list
                        attendanceList.add(new Attendance(prn, name, attendanceStatus));

                        // Notify the adapter to update the RecyclerView
                        attendanceAdapter.notifyDataSetChanged();
                    });
        }
    }
}