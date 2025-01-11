package com.example.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;

public class RegisterActivity extends AppCompatActivity {

    private EditText idEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private Spinner roleSpinner;
    private Button registerButton, fingerprintRegisterButton;
    private String selectedRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        idEditText = findViewById(R.id.idEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        roleSpinner = findViewById(R.id.roleSpinner);
        registerButton = findViewById(R.id.registerButton);
        fingerprintRegisterButton = findViewById(R.id.fingerprintRegisterButton);

        // Set listener for role selection
        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                selectedRole = parentView.getItemAtPosition(position).toString();
                handleRoleSelection();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle the case when no item is selected
            }
        });

        // On clicking the fingerprint register button, prompt for fingerprint registration
        fingerprintRegisterButton.setOnClickListener(v -> registerFingerprint());

        // On clicking the submit button, register the user
        registerButton.setOnClickListener(v -> registerUser());
    }

    private void handleRoleSelection() {
        // If the selected role is "Student", show fingerprint registration
        if ("Student".equals(selectedRole)) {
            fingerprintRegisterButton.setVisibility(View.VISIBLE);
            registerButton.setVisibility(View.GONE);
        }
        // If the selected role is "Teacher", show the submit button directly
        else if ("Teacher".equals(selectedRole)) {
            fingerprintRegisterButton.setVisibility(View.GONE);
            registerButton.setVisibility(View.VISIBLE);
        }
    }

    private void registerUser() {
        String id = idEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Input validation
        if (id.isEmpty() || password.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please enter ID and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user with email and password using Firebase Authentication
        mAuth.createUserWithEmailAndPassword(id, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User is successfully registered
                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();

                        // Redirect based on role
                        if ("Teacher".equals(selectedRole)) {
                            Intent intent = new Intent(RegisterActivity.this, TeacherMainActivity.class);
                            startActivity(intent);
                        } else if ("Student".equals(selectedRole)) {
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        // Registration failed
                        Toast.makeText(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerFingerprint() {
        // Check if the device supports biometric authentication
        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);

        if (canAuthenticate == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE) {
            Toast.makeText(this, "No biometric hardware available", Toast.LENGTH_SHORT).show();
            return;
        } else if (canAuthenticate == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE) {
            Toast.makeText(this, "Biometric hardware is unavailable", Toast.LENGTH_SHORT).show();
            return;
        } else if (canAuthenticate == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
            Toast.makeText(this, "No biometric enrolled. Please register your fingerprint.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Biometric prompt for fingerprint registration
        BiometricPrompt biometricPrompt = new BiometricPrompt(RegisterActivity.this,
                ContextCompat.getMainExecutor(RegisterActivity.this), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // Fingerprint registered successfully
                Toast.makeText(RegisterActivity.this, "Fingerprint registered successfully!", Toast.LENGTH_SHORT).show();

                // Show the submit button after successful fingerprint registration
                registerButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(RegisterActivity.this, "Fingerprint registration failed.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up BiometricPrompt for fingerprint registration
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Authentication")
                .setDescription("Please register your fingerprint.")
                .setNegativeButtonText("Cancel")
                .build();
        biometricPrompt.authenticate(promptInfo);
    }
}