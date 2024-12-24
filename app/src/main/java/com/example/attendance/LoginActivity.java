package com.example.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText idEditText, passwordEditText;
    private Spinner roleSpinner;
    private Button loginButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        // Initialize Views
        idEditText = findViewById(R.id.idEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        roleSpinner = findViewById(R.id.roleSpinner);
        loginButton = findViewById(R.id.submitButton);

        // On clicking the login button, authenticate the user
        loginButton.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String Email = idEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String selectedRole = roleSpinner.getSelectedItem().toString();

        // Input validation
        if (Email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please enter ID and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Authenticate using Firebase Authentication
        mAuth.signInWithEmailAndPassword(Email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Check role and navigate accordingly
                            if (selectedRole.equals("Teacher")) {
                                Intent intent = new Intent(LoginActivity.this, TeacherMainActivity.class);
                                startActivity(intent);
                                finish();  // To close the LoginActivity once logged in
                            } else if (selectedRole.equals("Student")) {
                                Intent intent = new Intent(LoginActivity.this, StudentMainActivity.class);
                                startActivity(intent);
                                finish();  // To close the LoginActivity once logged in
                            }
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}