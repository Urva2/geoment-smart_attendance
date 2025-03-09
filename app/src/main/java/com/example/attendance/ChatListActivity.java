package com.example.attendance;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatListActivity extends AppCompatActivity {
    RecyclerView recyclerChatList;
    FirebaseFirestore db;
    String userRole, branch, year, department, userID;
    List<String> subjectList;
    ChatListAdapter chatListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        userRole = getIntent().getStringExtra("userRole");
        recyclerChatList = findViewById(R.id.recyclerChatList);
        db = FirebaseFirestore.getInstance();
        if ("Student".equals(userRole)) {
            SharedPreferences sharedPreferences = getSharedPreferences("StudentInfo", Context.MODE_PRIVATE);
            //IF SOME ERROR IN FETCHING THEN CHANGE getActivity() TO requireContext() and Context TO getActivity()
            // studentPRN = sharedPreferences.getString("PRN", "");
            department = sharedPreferences.getString("Department", "");
            year = sharedPreferences.getString("Year", "");
            Toast.makeText(this,"Please select your subject",Toast.LENGTH_SHORT).show();
        } else {
            department = getIntent().getStringExtra("department");

            Toast.makeText(this, department + userRole, Toast.LENGTH_LONG).show();
        }


        // Get details from intent

        //branch = getIntent().getStringExtra("branch");
        //year = getIntent().getStringExtra("year");

        //userID = FirebaseAuth.getInstance().getUid();

        // Debugging
        if ("Student".equals(userRole)) {
            if (userRole == null || year == null || department == null) {
                Toast.makeText(this, "Error: Missing user details", Toast.LENGTH_LONG).show();
                return;
            }
        }

        subjectList = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(subjectList, this, userRole);
        recyclerChatList.setLayoutManager(new LinearLayoutManager(this));
        recyclerChatList.setAdapter(chatListAdapter);

        loadSubjects();
    }



    private void loadSubjects() {
        if ("Student".equals(userRole)) {
            if (department == null || year == null) {
                Toast.makeText(this, "Error: department or year is null", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if ("Student".equals(userRole)) {
            db.collection("subjects").document(department).collection(year).document(year)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<String> subjects = (List<String>) documentSnapshot.get("subject"); // Get array
                            if (subjects != null && !subjects.isEmpty()) {
                                subjectList.clear();
                                subjectList.addAll(subjects);
                                chatListAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(this, "Subjects array is empty", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Document does not exist", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to load subjects: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
        else {
            // For teachers: manually fetch subjects from known year collections
            String[] years = {"FY", "SY", "TY"};
            for (String yr : years) {
                db.collection("subjects").document(department)
                        .collection(yr).document(yr)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                List<String> subjects = (List<String>) documentSnapshot.get("subject");
                                if (subjects != null && !subjects.isEmpty()) {
                                    subjectList.addAll(subjects);
                                    chatListAdapter.notifyDataSetChanged();
                                }
                            }
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed to load subjects for " + yr + ": " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

        }


    }
}