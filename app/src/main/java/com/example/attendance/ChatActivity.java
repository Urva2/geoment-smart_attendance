package com.example.attendance;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    RecyclerView recyclerMessages;
    EditText etMessage;
    TextView tvSubjectName;
    ImageButton btnSend;
    FirebaseFirestore db;
    FirebaseAuth auth;
    String subjectID, userRole, userID, userName;
    List<MessageModel> messageList;
    ChatAdapter chatAdapter;
    private ListenerRegistration chatListener; // Firestore Listener

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerMessages = findViewById(R.id.recyclerMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userID = auth.getUid();
        tvSubjectName=findViewById(R.id.tvSubjectName);
        Toolbar chatToolbar=findViewById(R.id.chatToolbar);


        // Get data from intent
        subjectID = getIntent().getStringExtra("subjectID");
        setSupportActionBar(chatToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back button
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back); // Custom back icon (optional)

        if(subjectID!=null)
        {
            tvSubjectName.setText(subjectID);
        }
        //userRole = getIntent().getStringExtra("userRole");
      //  userName = getIntent().getStringExtra("userName");
        SharedPreferences sharedPreferences = getSharedPreferences("StudentInfo", Context.MODE_PRIVATE);
        //IF SOME ERROR IN FETCHING THEN CHANGE getActivity() TO requireContext() and Context TO getActivity()
        // studentPRN = sharedPreferences.getString("PRN", "");
        //department = sharedPreferences.getString("Department", "");
        //year = sharedPreferences.getString("Year","");
        userRole = sharedPreferences.getString("userRole","");
        userName  = sharedPreferences.getString("studentName","");
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, userID);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(chatAdapter);

        loadMessages();

        btnSend.setOnClickListener(v -> sendMessage());
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Navigate back
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadMessages() {
        chatListener = db.collection("chats").document(subjectID).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading messages", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value == null || value.isEmpty()) return; // Null check

                    messageList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        MessageModel message = doc.toObject(MessageModel.class);
                        if (message != null) {
                            messageList.add(message);
                        }
                    }

                    chatAdapter.notifyDataSetChanged();
                    recyclerMessages.scrollToPosition(messageList.size() - 1);
                });
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty() || messageText.replaceAll("\\s", "").isEmpty()) {
            Toast.makeText(this, "Cannot send an empty message", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("message", messageText);
        messageData.put("senderID", userID);
        messageData.put("senderName", userName);
        messageData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("chats").document(subjectID).collection("messages")
                .add(messageData)
                .addOnSuccessListener(docRef -> etMessage.setText(""))
                .addOnFailureListener(e -> Toast.makeText(this, "Message failed to send", Toast.LENGTH_SHORT).show());
    }
    private String formatTimestamp(Timestamp timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (chatListener != null) {
            chatListener.remove(); // Prevent memory leaks
 }
}
}
