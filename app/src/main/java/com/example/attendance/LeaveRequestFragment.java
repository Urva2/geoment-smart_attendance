package com.example.attendance;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class LeaveRequestFragment extends Fragment {

    private static final int PICK_PDF_REQUEST = 1;
    private Uri pdfUri;
    private TextView txtPdfName;
    private ProgressBar progressBar;
    private EditText editReason, editStartDate, editEndDate;
    private Button btnSelectPdf, btnSubmitLeave;
    private FirebaseFirestore db;
    private String studentBranch = "", studentYear = "", studentPRN = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leave_request, container, false);

        editReason = view.findViewById(R.id.editReason);
        editStartDate = view.findViewById(R.id.editStartDate);
        editEndDate = view.findViewById(R.id.editEndDate);
        btnSelectPdf = view.findViewById(R.id.btnSelectPdf);
        btnSubmitLeave = view.findViewById(R.id.btnSubmitLeave);
        db = FirebaseFirestore.getInstance();
        txtPdfName = view.findViewById(R.id.txtPdfName);
        progressBar = view.findViewById(R.id.progressBar);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("StudentInfo", Context.MODE_PRIVATE);
        //IF SOME ERROR IN FETCHING THEN CHANGE getActivity() TO requireContext() and Context TO getActivity()
        studentPRN = sharedPreferences.getString("PRN", "");
        studentBranch = sharedPreferences.getString("Department", "");
        studentYear = sharedPreferences.getString("Year","");
        Toast.makeText(getContext(), "Found student in " + studentYear + " of " + studentBranch, Toast.LENGTH_SHORT).show();
        // Retrieve PRN from SharedPreferences
       /* studentPRN = getActivity().getSharedPreferences("UserPrefs", getActivity().MODE_PRIVATE).getString("userPRN", "");

        if (!studentPRN.isEmpty()) {
            fetchStudentDetails(studentPRN);
        } else {
            Toast.makeText(getContext(), "PRN not found. Please try again.", Toast.LENGTH_SHORT).show();
        }*/

        editStartDate.setOnClickListener(v -> showDatePickerDialog(editStartDate));
        editEndDate.setOnClickListener(v -> showDatePickerDialog(editEndDate));
        btnSelectPdf.setOnClickListener(v -> selectPdf());
        btnSubmitLeave.setOnClickListener(v -> uploadPdfToCloudinary());

        return view;
    }

    private void showDatePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    editText.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

   /* private void fetchStudentDetails(String prn) {
        db.collection("studentdetails")
                .get()
                .addOnSuccessListener(branchSnapshots -> {
                    boolean[] prnFound = {false};
                    for (QueryDocumentSnapshot branchDoc : branchSnapshots) {
                        String branchName = branchDoc.getId();
                        for (String yearOption : getResources().getStringArray(R.array.year_options)) {
                            db.collection("studentdetails").document(branchName)
                                    .collection(yearOption)
                                    .whereEqualTo("prn", prn)
                                    .get()
                                    .addOnSuccessListener(yearSnapshots -> {
                                        if (!yearSnapshots.isEmpty() && !prnFound[0]) {
                                            prnFound[0] = true;
                                            for (QueryDocumentSnapshot studentDoc : yearSnapshots) {
                                                studentBranch = studentDoc.getString("branch");
                                                studentYear = studentDoc.getString("year");
                                                Toast.makeText(getContext(), "Found student in " + studentYear + " of " + studentBranch, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Error fetching student details: " + e.getMessage()));
    }  */

    private void selectPdf() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, PICK_PDF_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            pdfUri = data.getData();
            String fileName = getFileNameFromUri(pdfUri);
            txtPdfName.setText(fileName);
            txtPdfName.setVisibility(View.VISIBLE);
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void uploadPdfToCloudinary() {
        if (pdfUri == null) {
            Toast.makeText(getContext(), "Select a PDF first", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Map<String, Object> config = new HashMap<>();
                config.put("cloud_name", "ds12ivy94");
                config.put("api_key", "356955384385763");
                config.put("api_secret", "E0-zZv_zjqEeuq4nJY1pRmxbFlE");
                Cloudinary cloudinary = new Cloudinary(config);

                File file = getFileFromUri(pdfUri);
                if (file == null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Failed to get file", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    });
                    return;
                }

                Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap("resource_type", "raw"));
                String pdfUrl = (String) uploadResult.get("secure_url");

                getActivity().runOnUiThread(() -> {
                    submitLeaveRequest(pdfUrl);
                    progressBar.setVisibility(View.GONE);
                });

            } catch (Exception e) {
                Log.e("CloudinaryUpload", "Error: " + e.getMessage(), e);
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "PDF Upload Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }

    private File getFileFromUri(Uri uri) throws IOException {
        InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
        if (inputStream == null) return null;

        File tempFile = new File(getActivity().getCacheDir(), "temp_pdf_" + System.currentTimeMillis() + ".pdf");
        FileOutputStream outputStream = new FileOutputStream(tempFile);

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.close();
        inputStream.close();
        return tempFile;
    }

    private void submitLeaveRequest(String pdfUrl) {
        if (studentBranch.isEmpty() || studentYear.isEmpty() || studentPRN.isEmpty()) {
            Toast.makeText(getContext(), "Student details not fetched. Try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> leaveRequest = new HashMap<>();
        leaveRequest.put("startDate", editStartDate.getText().toString().trim());
        leaveRequest.put("endDate", editEndDate.getText().toString().trim());
        leaveRequest.put("reason", editReason.getText().toString().trim());
        leaveRequest.put("documentUrl", pdfUrl);
        leaveRequest.put("status", "Pending");
        leaveRequest.put("PRN", studentPRN);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("leave_requests").document(studentBranch)
                .collection(studentYear).document(studentPRN)
                .set(leaveRequest)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Leave Request Submitted", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Failed to Submit", Toast.LENGTH_SHORT).show()
                       );
    }

}
