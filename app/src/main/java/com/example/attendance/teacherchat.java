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
        replyMessageEditText = findViewById(R.id.replyMessageEditText);
        showMessagesButton = findViewById(R.id.showMessagesButton);
        sendReplyButton = findViewById(R.id.sendReplyButton);
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
        sendReplyButton.setOnClickListener(v -> {
            String prn = prnEditText.getText().toString().trim();
            String replyMessage = replyMessageEditText.getText().toString().trim();

            if (prn.isEmpty() || replyMessage.isEmpty()) {
                Toast.makeText(teacherchat.this, "Please enter PRN and reply message", Toast.LENGTH_SHORT).show();
            } else {
                sendReplyToStudent(prn, replyMessage);
            }
        });
    }

    private void showMessages(String lectureId) {
        db.collection("Subjects")
                .document(lectureId) // Subject ID
                .collection("Messages")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    messageListLayout.removeAllViews(); // Clear previous messages

                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(teacherchat.this, "No messages found for this Lecture ID", Toast.LENGTH_SHORT).show();
                    } else {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            // Fetch messages dynamically based on document ID
                            String studentPrn = documentSnapshot.getString("studentPrn");
                            String messageContent = documentSnapshot.getString("messageContent");

                            if (studentPrn != null && messageContent != null) {
                                // Creating a formatted message
                                TextView messageTextView = new TextView(teacherchat.this);
                                messageTextView.setText("PRN: " + studentPrn + "\nMessage: " + messageContent);
                                messageTextView.setPadding(10, 10, 10, 10);
                                messageTextView.setTextSize(16);
                                messageListLayout.addView(messageTextView);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(teacherchat.this, "Error loading messages", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendReplyToStudent(String prn, String replyMessage) {
        db.collection("Subjects")
                .document(lectureIdEditText.getText().toString()) // Subject ID
                .collection("Messages")
                .add(new Message(prn, replyMessage)) // Send reply message to specific student
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(teacherchat.this, "Reply sent to student!", Toast.LENGTH_SHORT).show();
                    replyMessageEditText.setText(""); // Clear reply field
                    prnEditText.setText(""); // Clear PRN field
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(teacherchat.this, "Error sending reply", Toast.LENGTH_SHORT).show();
                });
    }

    // Message class to represent a message object
    public static class Message {
        private String studentPrn;
        private String messageContent;

        public Message(String studentPrn, String messageContent) {
            this.studentPrn = studentPrn;
            this.messageContent = messageContent;
        }

        public String getStudentPrn() {
            return studentPrn;
        }

        public String getMessageContent() {
            return messageContent;
        }
    }
}