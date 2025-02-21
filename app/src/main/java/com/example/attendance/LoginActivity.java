package com.example.attendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private TextView tvsignup;
    private EditText etID, etPassword;
    private Button btnSubmit;
    private TextView errorTextView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI components
        etID = findViewById(R.id.etID);
        etPassword = findViewById(R.id.etPassword);
        btnSubmit = findViewById(R.id.btnSubmit);
        errorTextView = findViewById(R.id.errorTextView);
        tvsignup=findViewById(R.id.tvSignUpLink);

        // Initialize Firebase Authentication & Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(view -> showForgotPasswordDialog());

        // Check if user is already logged in and redirect accordingly
        checkIfUserIsLoggedIn();

        // Set Submit button click listener
        btnSubmit.setOnClickListener(view -> authenticateUser());
        tvsignup.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

    }

    private void checkIfUserIsLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String savedRole = sharedPreferences.getString("role", null);

        if (currentUser != null && isLoggedIn && savedRole != null) {
            navigateToMainScreen(savedRole);
        }
    }

    private void authenticateUser() {
        String userID = etID.getText().toString().trim();
        String userPassword = etPassword.getText().toString().trim();

        // Clear previous errors
        errorTextView.setVisibility(TextView.GONE);

        // Validate input fields
        if (userID.isEmpty() || userPassword.isEmpty()) {
            showError("Please enter both Email and Password");
            return;
        }

        // Authenticate user with Firebase Authentication
        mAuth.signInWithEmailAndPassword(userID, userPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            fetchUserRole(user.getUid());
                        }
                    } else {
                        showError("Authentication Failed. Please check your credentials.");
                    }
                });
    }

    private void fetchUserRole(String userID) {
        db.collection("users").document(userID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String role = task.getResult().getString("role");

                        if (role != null) {
                            // Save login state and role in SharedPreferences
                            sharedPreferences.edit()
                                    .putBoolean("isLoggedIn", true)
                                    .putString("role", role)
                                    .apply();

                            // Navigate to the respective screen
                            navigateToMainScreen(role);
                        } else {
                            showError("Role not found. Please contact the administrator.");
                        }
                    } else {
                        showError("Error fetching role. Try again later.");
                    }
                });
    }

    private void navigateToMainScreen(String role) {
        Intent intent;
        if ("Student".equals(role)) {
            intent = new Intent(LoginActivity.this, StudentMainActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, TeacherMainActivity.class);
        }
        startActivity(intent);
        finish(); // Prevents user from going back to login screen
    }

    private void showError(String message) {
        errorTextView.setVisibility(TextView.VISIBLE);
        errorTextView.setText(message);
        errorTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
    }
    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        final EditText emailInput = new EditText(this);
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setHint("Enter your registered email");
        builder.setView(emailInput);

        builder.setPositiveButton("Send Reset Email", (dialog, which) -> {
            String email = emailInput.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Error! Email not found", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

}