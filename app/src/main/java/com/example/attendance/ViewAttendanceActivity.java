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
import java.util.ArrayList;
import java.util.List;

public class ViewAttendanceActivity extends AppCompatActivity {

    private EditText editTextId;
    private Button buttonShow;
    private RecyclerView recyclerView;
    private FirebaseFirestore firestore;
    private List<Student> studentList;
    private StudentAdapter adapter;

    private String selectedYear;
    private String selectedBranch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        // Initialize Views
        editTextId = findViewById(R.id.editTextId);
        buttonShow = findViewById(R.id.buttonShow);
        recyclerView = findViewById(R.id.recyclerView);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();
        studentList = new ArrayList<>();
        adapter = new StudentAdapter(studentList);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Handle "Show" Button Click
        buttonShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredId = editTextId.getText().toString().trim();
                if (enteredId.isEmpty()) {
                    Toast.makeText(ViewAttendanceActivity.this, "Enter a valid ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                fetchAppointmentDetails(enteredId);
            }
        });
    }

    private void fetchAppointmentDetails(String enteredId) {
        firestore.collection("appointmentdetails")
                .whereEqualTo("id", enteredId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            selectedYear = document.getString("year");
                            selectedBranch = document.getString("branch");
                            fetchStudentDetails();
                            break;
                        }
                    } else {
                        Toast.makeText(ViewAttendanceActivity.this, "No matching ID found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(ViewAttendanceActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show());
    }


    private void fetchStudentDetails() {
        firestore.collection("studentdetails")
                .whereEqualTo("year", selectedYear)
                .whereEqualTo("branch", selectedBranch)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    studentList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("name");
                        String prn = document.getString("prn");
                        studentList.add(new Student(name, prn, "A")); // Default to Absent
                    }
                    checkAttendance();
                })
                .addOnFailureListener(e -> Toast.makeText(ViewAttendanceActivity.this, "Error fetching student details", Toast.LENGTH_SHORT).show());
    }

    private void checkAttendance() {
        for (int i = 0; i < studentList.size(); i++) {
            final int index = i;
            String prn = studentList.get(i).getPrn();
            firestore.collection("attendance")
                    .whereEqualTo("prn", prn)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            studentList.get(index).setAttendance("P"); // Mark as Present
                        }
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Toast.makeText(ViewAttendanceActivity.this, "Error checking attendance", Toast.LENGTH_SHORT).show());
        }
    }
}
