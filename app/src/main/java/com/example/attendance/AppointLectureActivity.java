package com.example.attendance;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AppointLectureActivity extends AppCompatActivity {

    private EditText editTextDate, editTextTime, editTextLectureName, editTextID;
    private Spinner spinnerBranch, spinnerYear;
    private Button btnAppoint;
    // Firestore instance
    private FirebaseFirestore firestore;

    // Array for branch and year (assumed predefined)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appoint_lecture);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize views
        TextView textViewGreeting = findViewById(R.id.textViewGreeting);
        TextView textViewDepartment = findViewById(R.id.textViewDepartment);
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);
        editTextLectureName = findViewById(R.id.editTextLectureName);
        editTextID = findViewById(R.id.editTextID);
        //spinnerBranch = findViewById(R.id.spinnerBranch);
        spinnerYear = findViewById(R.id.spinnerYear);
        btnAppoint = findViewById(R.id.btnAppoint);

        // SharedPreferences sharedPreferences = getSharedPreferences("TeacherInfo", Context.MODE_PRIVATE);
        String teacherName =  getIntent().getStringExtra("name");
        String department = getIntent().getStringExtra("department");

        // Set the greeting text
        textViewGreeting.setText("Hello, " + teacherName);
        textViewDepartment.setText("Department: " + department);

        // Date and Time Pickers
        editTextDate.setOnClickListener(v -> showDatePickerDialog(editTextDate));
        editTextTime.setOnClickListener(v -> showTimePickerDialog(editTextTime));

        btnAppoint.setOnClickListener(v -> {
            String date = editTextDate.getText().toString().trim();
            String time = editTextTime.getText().toString().trim();
            String lectureName = editTextLectureName.getText().toString().trim();
            String id = editTextID.getText().toString().trim();
            // String branch = spinnerBranch.getSelectedItem().toString().trim();
            String year = spinnerYear.getSelectedItem().toString().trim();

            // Validate inputs
            if (TextUtils.isEmpty(date) || TextUtils.isEmpty(time) ||
                    TextUtils.isEmpty(lectureName) || TextUtils.isEmpty(id)) {
                Toast.makeText(AppointLectureActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ensure Lecture ID contains only digits and has a maximum length of 2
            if (!id.matches("\\d{1,2}")) {
                Toast.makeText(AppointLectureActivity.this, "Lecture ID must be 1 or 2 digits only!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Store data in Firestore
            Map<String, Object> lectureData = new HashMap<>();
            lectureData.put("date", date);
            lectureData.put("time", time);
            lectureData.put("namelec", lectureName);
            lectureData.put("id", id);
            lectureData.put("branch", department);
            lectureData.put("year", year);
            lectureData.put("department", department);

            firestore.collection("appointmentdetails").document(department)
                    .collection(year)
                    .document(lectureName)
                    .collection(id).document(id)
                    .set(lectureData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(AppointLectureActivity.this, "Lecture appointed successfully!", Toast.LENGTH_SHORT).show();
                        clearFields();

                        Intent intent = new Intent(AppointLectureActivity.this, TeacherMainActivity.class);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AppointLectureActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
    // Clear input fields after successful data entry
    private void clearFields() {
        editTextDate.setText("");
        editTextTime.setText("");
        editTextLectureName.setText("");
        editTextID.setText("");
        //spinnerBranch.setSelection(0);
        spinnerYear.setSelection(0);
    }


    private void showTimePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(AppointLectureActivity.this,
                (view, selectedHour, selectedMinute) -> {
                    String selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                    editText.setText(selectedTime);
                },
                hour, minute, true // true for 24-hour format, false for AM/PM format
        );
        timePickerDialog.show();
    }
    private void showDatePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(AppointLectureActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    editText.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }
}