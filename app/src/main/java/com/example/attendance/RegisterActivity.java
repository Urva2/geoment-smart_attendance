package com.example.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executor;

public class RegisterActivity extends AppCompatActivity {

    private EditText idEditText, passwordEditText;
    private RadioGroup roleRadioGroup;
    private Button nextButton, registerFingerprintButton;
    private FirebaseAuth mAuth;
    private TextView tvSignInLink;
    private FirebaseFirestore firestore;

    private boolean isFingerprintRegistered = false; // Track if fingerprint is registered

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize FirebaseAuth and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize Views
        idEditText = findViewById(R.id.idEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        nextButton = findViewById(R.id.nextButton);
        registerFingerprintButton = findViewById(R.id.registerFingerprintButton);
        tvSignInLink=findViewById(R.id.tvSignInLink);

        // Initially hide the fingerprint button
        registerFingerprintButton.setVisibility(Button.GONE);
        tvSignInLink.setOnClickListener(view -> navigateToSignIn());




        // Set radio group change listener
        roleRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRoleButton = findViewById(checkedId);
            String selectedRole = selectedRoleButton.getText().toString();
            if (selectedRole.equals("Student")) {
                // Show fingerprint button for students
                registerFingerprintButton.setVisibility(Button.VISIBLE);
                nextButton.setEnabled(false); // Disable next button until fingerprint is registered
            } else {
                // Hide fingerprint button for teachers
                registerFingerprintButton.setVisibility(Button.GONE);
                nextButton.setEnabled(true); // Enable next button for teachers
            }
        });

        // Fingerprint registration button click listener
        registerFingerprintButton.setOnClickListener(v -> registerFingerprint());

        // Next Button Click Listener
        nextButton.setOnClickListener(v -> registerUser());
    }

    private void navigateToSignIn() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class); // Replace with your Sign Up activity
        startActivity(intent);
    }

    private void registerFingerprint() {
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, "Your device does not support fingerprint registration", Toast.LENGTH_SHORT).show();
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(RegisterActivity.this, "Fingerprint registered successfully!", Toast.LENGTH_SHORT).show();
                isFingerprintRegistered = true;
                nextButton.setEnabled(true); // Enable the next button
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(RegisterActivity.this, "Error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(RegisterActivity.this, "Fingerprint registration failed", Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Registration")
                .setSubtitle("Register your fingerprint")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void registerUser() {
        String id = idEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        int selectedRoleId = roleRadioGroup.getCheckedRadioButtonId();

        // Validation
        if (id.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter Email and Password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedRoleId == -1) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRoleButton = findViewById(selectedRoleId);
        String selectedRole = selectedRoleButton.getText().toString();

        if (selectedRole.equals("Student") && !isFingerprintRegistered) {
            Toast.makeText(this, "Please register your fingerprint before submitting", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(id, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Store the role and email in Firestore
                            storeUserDataInFirestore(user.getUid(), user.getEmail(), selectedRole);

                            Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();

                            // Redirect based on role
                            Intent intent;
                            if (selectedRole.equals("Student")) {
                                intent = new Intent(RegisterActivity.this, StudentMainActivity1.class);
                            } else {
                                intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            }
                            startActivity(intent);
                            finish(); // Prevents user from going back to registration screen
                        }
                    } else {
                        // Show error message
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(RegisterActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void storeUserDataInFirestore(String userId, String email, String role) {
        // Create a new user document with the email and role
        User user = new User(email, role);

        // Save the user data in Firestore under "users" collection
        firestore.collection("users")
                .document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    // Data saved successfully
                })
                .addOnFailureListener(e -> {
                    // Error occurred while saving data
                    Toast.makeText(RegisterActivity.this, "Error saving user data", Toast.LENGTH_SHORT).show();
                });
    }

    public static class User {
        private String email;
        private String role;

        public User(String email, String role) {
            this.email = email;
            this.role = role;
        }

        public String getEmail() {
            return email;
        }

        public String getRole() {
            return role;
        }
    }
}

