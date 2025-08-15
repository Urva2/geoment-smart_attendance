package com.example.attendance;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherLeaveActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LeaveRequestAdapter adapter;
    private FirebaseFirestore db;
    private List<LeaveRequest> leaveRequestsList = new ArrayList<>();
    private EditText editBranch, editYear;
    private Button btnFetchRequests;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "TeacherPrefs";
    private static final String KEY_BRANCH = "branch";
    private static final String KEY_YEAR = "year";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_leave);

        editBranch = findViewById(R.id.editBranch);
        editYear = findViewById(R.id.editYear);
        btnFetchRequests = findViewById(R.id.btnFetchRequests);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LeaveRequestAdapter(this, leaveRequestsList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load previously selected branch and year
        editBranch.setText(sharedPreferences.getString(KEY_BRANCH, ""));
        editYear.setText(sharedPreferences.getString(KEY_YEAR, ""));

        btnFetchRequests.setOnClickListener(v -> {
            String branch = editBranch.getText().toString().trim();
            String year = editYear.getText().toString().trim();

            if (!branch.isEmpty() && !year.isEmpty()) {
                // Save branch and year to SharedPreferences
                sharedPreferences.edit()
                        .putString(KEY_BRANCH, branch)
                        .putString(KEY_YEAR, year)
                        .apply();

                fetchLeaveRequests(branch, year);
            }
        });
    }

    private void fetchLeaveRequests(String branch, String year) {
        db.collection("leave_requests").document(branch).collection(year)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        leaveRequestsList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            LeaveRequest leaveRequest = document.toObject(LeaveRequest.class);
                            if (leaveRequest != null) {
                                leaveRequest.setPRN(document.getId());
                            }
                            leaveRequestsList.add(leaveRequest);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("FirestoreError", "Failed to fetch leave requests", task.getException());
                    }
           });
}
}
