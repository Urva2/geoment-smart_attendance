package com.example.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etID, etPassword;
    private TextView tvSignUpLink;
    private Button btnSubmit;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI components
        etID = findViewById(R.id.etID);
        etPassword = findViewById(R.id.etPassword);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvSignUpLink = findViewById(R.id.tvSignUpLink);


        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set Submit button click listener
        btnSubmit.setOnClickListener(view -> authenticateUser());
        tvSignUpLink.setOnClickListener(view -> navigateToSignUp());

    }

    private void navigateToSignUp() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class); // Replace with your Sign Up activity
        startActivity(intent);
    }

    private void authenticateUser() {
        String userID = etID.getText().toString().trim();
        String userPassword = etPassword.getText().toString().trim();

        // Validate input fields
        if (userID.isEmpty() || userPassword.isEmpty()) {
            Toast.makeText(this, "Please enter both Email and Password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Authenticate user with Firebase Authentication
        mAuth.signInWithEmailAndPassword(userID, userPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Get the current authenticated user
                        String currentUserID = mAuth.getCurrentUser().getUid();

                        // Fetch role from Firestore based on userID (UID)
                        db.collection("users")
                                .document(currentUserID)  // Use UID to get the user document
                                .get()
                                .addOnCompleteListener(queryTask -> {
                                    if (queryTask.isSuccessful() && queryTask.getResult() != null) {
                                        String role = queryTask.getResult().getString("role");

                                        if (role != null) {
                                            // Navigate based on role
                                            Intent intent;
                                            if ("Student".equals(role)) {
                                                intent = new Intent(LoginActivity.this, StudentMainActivity.class);
                                            } else {
                                                intent = new Intent(LoginActivity.this, TeacherMainActivity.class);
                                            }
                                            startActivity(intent);
                                            finish(); // Prevents user from going back to login screen
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Role not found", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Error fetching role", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });






    }
}