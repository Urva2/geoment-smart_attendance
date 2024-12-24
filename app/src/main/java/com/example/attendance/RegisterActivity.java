package com.example.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText idEditText, passwordEditText;
    private Spinner roleSpinner;

    private FirebaseAuth mAuth;

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
        Button registerButton = findViewById(R.id.registerButton);

        // On clicking the register button, register the user
        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String id = idEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String selectedRole = roleSpinner.getSelectedItem().toString();

        // Input validation
        if (id.isEmpty() || password.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please enter ID and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user with email and password using Firebase Authentication
        mAuth.createUserWithEmailAndPassword(id, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User is successfully registered, go to the next activity
                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();

                        // Redirect based on role
                        if (selectedRole.equals("Teacher")) {
                            Intent intent = new Intent(RegisterActivity.this, TeacherMainActivity.class);
                            startActivity(intent);
                        } else if (selectedRole.equals("Student")) {
                            Intent intent = new Intent(RegisterActivity.this, StudentMainActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        // Registration failed
                        Toast.makeText(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}