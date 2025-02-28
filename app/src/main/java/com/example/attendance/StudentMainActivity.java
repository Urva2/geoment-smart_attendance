package com.example.attendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class StudentMainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth auth;
    private DrawerLayout drawerLayout;
    String prn, branch, year, studentName, userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);

        auth = FirebaseAuth.getInstance();
        mAuth = FirebaseAuth.getInstance();
        getStudentData();

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = auth.getCurrentUser().getUid();


        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        prn = documentSnapshot.getString("prn");
                        userRole = documentSnapshot.getString("role");
                        Toast.makeText(this, "PRN: " + prn + " & " + userRole, Toast.LENGTH_LONG).show();
                    }
                });

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
                                                studentName = studentDoc.getString("name");
                                                Toast.makeText(this, "Found " + userRole + " in " + year + " of " + branch, Toast.LENGTH_SHORT).show();
                                            }
                                            SharedPreferences sharedPreferences = getSharedPreferences("StudentInfo", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString("PRN", prn);
                                            editor.putString("Department", branch);
                                            editor.putString("Year", year);
                                            editor.putString("userRole", userRole);
                                            editor.putString("studentName", studentName);
                                            editor.apply();
                                        }
                                    });
                        }
                    }
                });

        // Chat Button

        Button button1 = findViewById(R.id.button1);
        button1.setOnClickListener(v -> {
            Intent intent = new Intent(StudentMainActivity.this, ChatListActivity.class);
            intent.putExtra("userRole", userRole);
            startActivity(intent);
        });

        // Show/Book Lecture Button
        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(v -> {
            Intent intent = new Intent(StudentMainActivity.this, StudentMainActivity2.class);
            startActivity(intent);
        });

        // Sick Leave Application
        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(v -> {
            Intent intent = new Intent(StudentMainActivity.this, StudentLeaveActivity.class);
            startActivity(intent);
        });

        // Logout Button
        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> logoutUser());
    }

    private void logoutUser() {
        mAuth.signOut();
        sharedPreferences.edit()
                .putBoolean("isLoggedIn", false)
                .remove("role")
                .apply();
        Intent intent = new Intent(StudentMainActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void getStudentData() {
        // Placeholder for any additional logic
    }


}

