package com.example.attendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class TeacherMainActivity extends AppCompatActivity {

    private Button btnAppointLecture, btnViewPastAttendance, chat,leave,logoutButton;
    private FirebaseAuth auth;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    String Name,department,facultyID,teacherName,userRole;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_main);

        // Initialize buttons
        btnAppointLecture = findViewById(R.id.btnAppointLecture);
        btnViewPastAttendance = findViewById(R.id.btnViewPastAttendance);
        chat = findViewById(R.id.chat);
        leave = findViewById(R.id.leave);
        logoutButton = findViewById(R.id.logoutButton);
        auth = FirebaseAuth.getInstance();
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
// Getting Data For Chat Activity
            // Get Firestore instance
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            // Get current user ID
            String userId = auth.getCurrentUser().getUid();

            // Reference to the user's document
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Store retrieved data in variables
                            Name = documentSnapshot.getString("name");
                            facultyID = documentSnapshot.getString("facultyID");
                            department = documentSnapshot.getString("department");
                            userRole = documentSnapshot.getString("role");
                            SharedPreferences sharedPreferences = getSharedPreferences("StudentInfo", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("studentName", Name);
                            editor.putString("userRole", userRole);
                            editor.apply();
                            Toast.makeText(this, "Name: " + userRole + Name + "Faculty ID: " + facultyID + "Department: " + department, Toast.LENGTH_LONG).show();
                            chat.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Navigate to Teacher Chat Activity (Chat functionality)
                                    Intent intent = new Intent(TeacherMainActivity.this, ChatListActivity.class);
                                    //  intent.putExtra("subjectID",subjectID);
                                    //   intent.putExtra("studentPRN",studentPRN);
                                    intent.putExtra("name",Name);
                                    intent.putExtra("userRole","teacher");
                                    //    intent.putExtra("userName",teacherName);
                                    intent.putExtra("department",department);
                                    //   intent.putExtra("facultyID",facultyID);
                                    startActivity(intent);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error fetching data: " + e.getMessage());
                    });

        // Set click listeners for buttons
        btnAppointLecture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to AppointLectureActivity
                Intent intent = new Intent(TeacherMainActivity.this, AppointLectureActivity.class);
                startActivity(intent);
            }
        });
        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to AppointLectureActivity
                Intent intent = new Intent(TeacherMainActivity.this, TeacherLeaveActivity.class);
                startActivity(intent);
            }
        });

        btnViewPastAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ViewAttendanceActivity
                Intent intent = new Intent(TeacherMainActivity.this, ViewAttendanceActivity.class);
                startActivity(intent);
            }
        });
        logoutButton.setOnClickListener(v-> logoutUser()) ;
    }
    private void logoutUser() {
        // Sign out from Firebase
        mAuth.signOut();

        // Clear login state in SharedPreferences
        sharedPreferences.edit()
                .putBoolean("isLoggedIn", false)
                .remove("role")
                .apply();

        // Redirect to MainActivity
        Intent intent = new Intent(TeacherMainActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears backstack
        startActivity(intent);
        finish(); // Close currentactivity
    }




}