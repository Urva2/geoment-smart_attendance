package com.example.attendance;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class ViewAttendanceActivity extends AppCompatActivity {

    private EditText branchEditText, yearEditText, subjectEditText, subjectIdEditText;
    private Button submitButton, exportPdfButton;
    private RecyclerView attendanceRecyclerView;
    private FirebaseFirestore db;
    private AttendanceAdapter attendanceAdapter;
    private List<Attendance> attendanceList;
    private Spinner spinnerBranch, spinnerYear;
    // Launcher to get the user-selected file URI
    private ActivityResultLauncher<Intent> saveFileLauncher;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        // sharedPreferences = getSharedPreferences("TeacherInfo", Context.MODE_PRIVATE);
        String teacherName =  getIntent().getStringExtra("name");
        String department = getIntent().getStringExtra("department");
        TextView textViewGreeting = findViewById(R.id.textViewGreeting);
        TextView textViewDepartment = findViewById(R.id.textViewDepartment);
// Set the greeting text
        textViewGreeting.setText("Hello, " + teacherName);
        textViewDepartment.setText("Department: " + department);
        Toast.makeText(ViewAttendanceActivity.this, ""+department, Toast.LENGTH_SHORT).show();
        // Initialize Views
        // branchEditText = findViewById(R.id.branchEditText);
        // yearEditText = findViewById(R.id.yearEditText);
        subjectEditText = findViewById(R.id.subjectEditText);
        subjectIdEditText = findViewById(R.id.subjectIdEditText);
        submitButton = findViewById(R.id.submitButton);
        exportPdfButton = findViewById(R.id.exportPdfButton);
        attendanceRecyclerView = findViewById(R.id.attendanceRecyclerView);
        spinnerYear = findViewById(R.id.spinnerYear);
        // Initialize RecyclerView
        attendanceList = new ArrayList<>();
        attendanceAdapter = new AttendanceAdapter(attendanceList);
        attendanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        attendanceRecyclerView.setAdapter(attendanceAdapter);

        // Register ActivityResultLauncher for saving the file
        saveFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null) {
                            createAndSavePDF(attendanceList, fileUri);
                        }
                    }
                }
        );

        // Submit Button Click Listener
        submitButton.setOnClickListener(view -> {
            String year = spinnerYear.getSelectedItem().toString().trim();
            String subject = subjectEditText.getText().toString().trim();
            String subjectId = subjectIdEditText.getText().toString().trim();

            if (year.isEmpty() || subject.isEmpty() || subjectId.isEmpty()) {
                Toast.makeText(ViewAttendanceActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                checkIfLectureExists(department, year, subject, subjectId);
            }
        });
        // Export PDF Button Click Listener
        exportPdfButton.setOnClickListener(view -> {
            if (attendanceList.isEmpty()) {
                Toast.makeText(ViewAttendanceActivity.this, "No data to export", Toast.LENGTH_SHORT).show();
            } else {
                selectFileLocation();
            }
        });
    }
    private void checkIfLectureExists(String department, String year, String subject, String subjectId) {
        db.collection("appointmentdetails")
                .document(department)
                .collection(year)
                .document(subject)
                .collection(subjectId)
                .document(subjectId)  // Checking if the subjectID document exists
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Subject and Subject ID exist, proceed to fetch attendance
                        fetchAttendanceData(department, year, subject, subjectId);
                    } else {
                        Toast.makeText(this, "Lecture not appointed. Cannot fetch attendance.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error checking lecture appointment.", Toast.LENGTH_SHORT).show());
    }
    private void fetchAttendanceData(String department, String year, String subject, String subjectId) {
        // Fetch PRNs and Names from studentdetails collection
        db.collection("studentdetails")
                .document(department)
                .collection(year)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> prns = new ArrayList<>();
                        List<String> names = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            prns.add(document.getId());
                            names.add(document.getString("name"));
                        }

                        fetchAttendanceDetails(prns, names, department, year, subject, subjectId);
                    } else {
                        Toast.makeText(ViewAttendanceActivity.this, "Error fetching student details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchAttendanceDetails(List<String> prns, List<String> names, String department, String year, String subject, String subjectId) {
        attendanceList.clear();

        for (int i = 0; i < prns.size(); i++) {
            String prn = prns.get(i);
            String name = names.get(i);

            db.collection("attendancedetails")
                    .document(department)
                    .collection(year)
                    .document(subject)
                    .collection(subjectId)
                    .document(prn)
                    .get()
                    .addOnCompleteListener(task -> {
                        String attendanceStatus = "A"; // Default attendance is "A"
                        if (task.isSuccessful() && task.getResult().exists()) {
                            String attend = task.getResult().getString("attend");
                            if ("P".equals(attend)) {
                                attendanceStatus = "P";
                            }
                        }

                        attendanceList.add(new Attendance(prn, name, attendanceStatus));
                        attendanceAdapter.notifyDataSetChanged();
                    });
        }
    }

    private void selectFileLocation() {
        // Intent to allow the user to select a location to save the file
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "Attendance_Report.pdf");
        saveFileLauncher.launch(intent);
    }

    private void createAndSavePDF(List<Attendance> attendanceList, Uri fileUri) {
        Document document = new Document();
        try {
            // Open an output stream using the fileUri
            OutputStream outputStream = getContentResolver().openOutputStream(fileUri);
            if (outputStream == null) {
                Toast.makeText(this, "Could not open file for writing", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create PdfWriter instance
            PdfWriter.getInstance(document, outputStream);

            // Open the document
            document.open();

            // Start HTML content
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<html><head><style>");
            htmlContent.append("</style></head><body>");
            htmlContent.append("<h1>Attendance Report</h1>");
            htmlContent.append("<table>");
            htmlContent.append("<tr><th>PRN</th><th>Name</th><th>Attendance Status</th></tr>");

            // Loop through the attendance data and add each row
            for (Attendance attendance : attendanceList) {
                htmlContent.append("<tr>");
                htmlContent.append("<td>").append(attendance.getPrn()).append("</td>");
                htmlContent.append("<td>").append(attendance.getName()).append("</td>");
                htmlContent.append("<td>        ").append(attendance.getAttendanceStatus()).append("</td>");
                htmlContent.append("</tr>");
            }

            htmlContent.append("</table></body></html>");

            // Convert HTML to PDF using HTMLWorker (iText 5.x)
            HTMLWorker htmlWorker = new HTMLWorker(document);
            htmlWorker.parse(new StringReader(htmlContent.toString()));

            // Close the document
            document.close();
            Toast.makeText(this, "PDF saved successfully", Toast.LENGTH_SHORT).show();

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error exporting PDF", Toast.LENGTH_SHORT).show();
        }
    }
}