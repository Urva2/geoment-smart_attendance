package com.example.attendance;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.attendance.LeaveAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.attendance.LeaveAdapter;
import java.util.ArrayList;
import java.util.List;

public class FacultyLeaveActivity extends AppCompatActivity {
    private RecyclerView leaveRecyclerView;
    private FirebaseFirestore db;
    private String teacherID = "teacher123";  // Replace with actual teacher ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_leave);

        leaveRecyclerView = findViewById(R.id.leaveRecyclerView);
        leaveRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();

        fetchLeaveRequests();
    }

    private void fetchLeaveRequests() {
        db.collection("sick_leave_requests").document(teacherID).collection("requests")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    List<LeaveRequest> leaveList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        LeaveRequest leave = doc.toObject(LeaveRequest.class);
                        leave.setRequestID(doc.getId());
                        leaveList.add(leave);
                    }
                    LeaveAdapter adapter = new LeaveAdapter(leaveList);
                    leaveRecyclerView.setAdapter(adapter);
});
    }
}
