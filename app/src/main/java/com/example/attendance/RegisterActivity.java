package com.example.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    private ImageView passwordToggle;
    private TextView tvSignInLink, errorTextView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private boolean isFingerprintRegistered = false;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        idEditText = findViewById(R.id.idEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        nextButton = findViewById(R.id.nextButton);
        registerFingerprintButton = findViewById(R.id.registerFingerprintButton);
        passwordToggle = findViewById(R.id.passwordToggle);
        tvSignInLink = findViewById(R.id.tvSignInLink);
        errorTextView = findViewById(R.id.errorTextView);

        registerFingerprintButton.setVisibility(View.GONE);
        tvSignInLink.setOnClickListener(view -> navigateToSignIn());

        roleRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRoleButton = findViewById(checkedId);
            if (selectedRoleButton.getText().toString().equals("Student")) {
                registerFingerprintButton.setVisibility(View.VISIBLE);
                nextButton.setEnabled(false);
            } else {
                registerFingerprintButton.setVisibility(View.GONE);
                nextButton.setEnabled(true);
            }
        });

        registerFingerprintButton.setOnClickListener(v -> registerFingerprint());

        passwordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordToggle.setImageResource(R.drawable.toggle2);
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordToggle.setImageResource(R.drawable.toggle);
            }
            isPasswordVisible = !isPasswordVisible;
            passwordEditText.setSelection(passwordEditText.length());
        });

        nextButton.setOnClickListener(v -> registerUser());
    }

    private void navigateToSignIn() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
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
                nextButton.setEnabled(true);
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

        errorTextView.setVisibility(View.GONE);
        errorTextView.setText("");

        StringBuilder errorMessages = new StringBuilder();

        if (id.isEmpty()) {
            errorMessages.append("• Email is required\n");
        }

        String passwordError = checkPasswordStrength(password);
        if (passwordError != null) {
            errorMessages.append(passwordError).append("\n");
        }

        if (selectedRoleId == -1) {
            errorMessages.append("• Please select a role\n");
        }

        if (errorMessages.length() > 0) {
            errorTextView.setText(errorMessages.toString().trim());
            errorTextView.setVisibility(View.VISIBLE);
            return;
        }

        RadioButton selectedRoleButton = findViewById(selectedRoleId);
        String selectedRole = selectedRoleButton.getText().toString();

        if (selectedRole.equals("Student") && !isFingerprintRegistered) {
            Toast.makeText(this, "Please register your fingerprint before submitting", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(id, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            storeUserDataInFirestore(user.getUid(), user.getEmail(), selectedRole);
                            Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        errorTextView.setText("• " + errorMessage);
                        errorTextView.setVisibility(View.VISIBLE);
                    }
                });
    }

    private String checkPasswordStrength(String password) {
        if (password.isEmpty()) return "• Password is required";
        if (password.length() < 8) return "• Password must be at least 8 characters long";
        boolean hasUppercase = false, hasLowercase = false, hasDigit = false, hasSpecialChar = false;
        String specialCharacters = "@#$%^&+=!";

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUppercase = true;
            if (Character.isLowerCase(c)) hasLowercase = true;
            if (Character.isDigit(c)) hasDigit = true;
            if (specialCharacters.contains(String.valueOf(c))) hasSpecialChar = true;
        }

        if (!hasUppercase) return "• Password must contain at least one uppercase letter (A-Z)";
        if (!hasLowercase) return "• Password must contain at least one lowercase letter (a-z)";
        if (!hasDigit) return "• Password must contain at least one number (0-9)";
        if (!hasSpecialChar) return "• Password must contain at least one special character (@, #, $, etc.)";

        return null;
    }

    private void storeUserDataInFirestore(String userId, String email, String role) {
        User user = new User(email, role);
        firestore.collection("users").document(userId).set(user).addOnFailureListener(e -> {
            errorTextView.setText("• Error saving user data");
            errorTextView.setVisibility(View.VISIBLE);
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