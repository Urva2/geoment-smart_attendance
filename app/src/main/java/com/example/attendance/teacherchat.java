package com.example.attendance;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class teacherchat extends AppCompatActivity {

    private EditText lectureIdEditText, prnEditText, replyMessageEditText;
    private Button showMessagesButton, sendReplyButton;
    private LinearLayout messageListLayout;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacherchat);

        db = FirebaseFirestore.getInstance();
        lectureIdEditText = findViewById(R.id.lectureIdEditText);
        prnEditText = findViewById(R.id.prnEditText);

        showMessagesButton = findViewById(R.id.showMessagesButton);

        messageListLayout = findViewById(R.id.messageListLayout);

        // Show messages button logic
        showMessagesButton.setOnClickListener(v -> {
            String lectureId = lectureIdEditText.getText().toString().trim();

            if (lectureId.isEmpty()) {
                Toast.makeText(teacherchat.this, "Please enter a Lecture ID", Toast.LENGTH_SHORT).show();
            } else {
                showMessages(lectureId);
            }
        });

        // Send reply button logic

    }

    private void showMessages(String lectureId) {
        db.collection("Subjects")
                .document(lectureId) // Subject ID
                .collection("Messages")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    messageListLayout.removeAllViews(); // Clear previous messages

                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(teacherchat.this, "No feedbacks found for this Lecture ID", Toast.LENGTH_SHORT).show();
                    } else {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            // Fetch messages dynamically based on document ID
                            String studentPrn = documentSnapshot.getString("studentPrn");
                            String messageContent = documentSnapshot.getString("messageContent");

                            if (studentPrn != null && messageContent != null) {
                                // Creating a formatted message
                                TextView messageTextView = new TextView(teacherchat.this);
                                messageTextView.setText("PRN: " + studentPrn + "\nFeedback: " + messageContent);
                                messageTextView.setPadding(10, 10, 10, 10);
                                messageTextView.setTextSize(16);
                                messageListLayout.addView(messageTextView);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(teacherchat.this, "Error loading feedbacks", Toast.LENGTH_SHORT).show();
                });
    }


    // Message class to represent a message object
}