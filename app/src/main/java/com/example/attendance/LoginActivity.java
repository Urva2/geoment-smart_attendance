package com.example.attendance;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.Button;
import android.widget.AdapterView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etID, etPassword;
    private Spinner spinnerUserType;
    private CheckBox cbStudent;
    private Button btnSubmit;
    private FirebaseAuth mAuth;
    private String selectedUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etID = findViewById(R.id.etID);
        etPassword = findViewById(R.id.etPassword);
        spinnerUserType = findViewById(R.id.spinnerUserType);
        cbStudent = findViewById(R.id.cbStudent);
        btnSubmit = findViewById(R.id.btnSubmit);

        mAuth = FirebaseAuth.getInstance();

        // Spinner onItemSelectedListener for user type (Student or Teacher)
        spinnerUserType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedUserType = parentView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                selectedUserType = null;
            }
        });

        // Submit button click listener
        btnSubmit.setOnClickListener(view -> authenticateUser());
    }

    private void authenticateUser() {
        String userID = etID.getText().toString().trim();
        String userPassword = etPassword.getText().toString().trim();

        if (userID.isEmpty() || userPassword.isEmpty()) {
            Toast.makeText(this, "Please enter both ID and Password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(userID, userPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Check selected user type and checkbox status
                            if ("Student".equals(selectedUserType)) {
                                if (cbStudent.isChecked()) {
                                    // Navigate to MainActivity1 (Student Main Activity 1)
                                    startActivity(new Intent(LoginActivity.this, StudentMainActivity1.class));
                                } else {
                                    // Navigate to MainActivity2 (Student Main Activity 2)
                                    startActivity(new Intent(LoginActivity.this, StudentMainActivity2.class));
                                }
                            } else if ("Teacher".equals(selectedUserType)) {
                                // Navigate to Teacher Main Activity
                                startActivity(new Intent(LoginActivity.this, TeacherMainActivity.class));
                            }
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}