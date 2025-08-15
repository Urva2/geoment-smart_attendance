package com.example.attendance;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LeaveStatusFragment extends Fragment {

    private FirebaseFirestore db;
    private TextView txtLeaveStatus;
    private String studentBranch, studentYear, studentPRN;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leave_status, container, false);

        txtLeaveStatus = view.findViewById(R.id.txtLeaveStatus);
        db = FirebaseFirestore.getInstance();

        // Get PRN from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("StudentInfo", Context.MODE_PRIVATE);
        //IF SOME ERROR IN FETCHING THEN CHANGE getActivity() TO requireContext() and Context TO getActivity()
        studentPRN = sharedPreferences.getString("PRN", "");
        studentBranch = sharedPreferences.getString("Department", "");
        studentYear = sharedPreferences.getString("Year","");
        Toast.makeText(getContext(), "PRN found."+studentPRN, Toast.LENGTH_SHORT).show();
        fetchLeaveStatus();
      /*  if (!studentPRN.isEmpty()) {
            fetchStudentDetails(studentPRN);
        } else {
            Toast.makeText(getContext(), "PRN not found. Please try again.", Toast.LENGTH_SHORT).show();
        }*/

        return view;
    }

    /*private void fetchStudentDetails(String prn) {
        db.collection("studentdetails")
                .get()
                .addOnSuccessListener(branchSnapshots -> {
                    boolean[] prnFound = {false};
                    for (DocumentSnapshot branchDoc : branchSnapshots) {
                        String branchName = branchDoc.getId();
                        for (String yearOption : getResources().getStringArray(R.array.year_options)) {
                            db.collection("studentdetails").document(branchName)
                                    .collection(yearOption)
                                    .whereEqualTo("prn", prn)
                                    .get()
                                    .addOnSuccessListener(yearSnapshots -> {
                                        if (!yearSnapshots.isEmpty() && !prnFound[0]) {
                                            prnFound[0] = true;
                                            for (DocumentSnapshot studentDoc : yearSnapshots) {
                                                studentBranch = studentDoc.getString("branch");
                                                studentYear = studentDoc.getString("year");
                                                fetchLeaveStatus();
                                            }
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Error fetching student details: " + e.getMessage()));
    }*/

    private void fetchLeaveStatus() {
        if (studentBranch == null || studentYear == null || studentPRN == null) {
            Toast.makeText(getContext(), "Student details not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("leave_requests").document(studentBranch)
                .collection(studentYear).document(studentPRN)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");
                        txtLeaveStatus.setText("Leave Status: " + status);
                    } else {
                        txtLeaveStatus.setText("No leave request found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch leave status", Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreError", "Error fetching leave status: " + e.getMessage());
           });
}
}
