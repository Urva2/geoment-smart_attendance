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

public class studentchat extends AppCompatActivity {

    private EditText lectureIdEditText, prnEditText, messageEditText;
    private Button sendMessageButton;
    private LinearLayout messageListLayout;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_studentchat);

        db = FirebaseFirestore.getInstance();
        lectureIdEditText = findViewById(R.id.lectureIdEditText);
        prnEditText = findViewById(R.id.prnEditText);
        messageEditText = findViewById(R.id.messageEditText);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        messageListLayout = findViewById(R.id.messageListLayout);

        // Send message logic
        sendMessageButton.setOnClickListener(v -> {
            String lectureId = lectureIdEditText.getText().toString().trim();
            String prn = prnEditText.getText().toString().trim();
            String message = messageEditText.getText().toString().trim();

            if (lectureId.isEmpty() || prn.isEmpty() || message.isEmpty()) {
                Toast.makeText(studentchat.this, "Please enter all details", Toast.LENGTH_SHORT).show();
            } else {
                sendMessageToTeacher(lectureId, prn, message);
            }
        });

        // Fetch messages for the student based on PRN
        fetchMessagesForStudent();
    }

    private void sendMessageToTeacher(String lectureId, String prn, String message) {
        // Sending message logic (store message in Firestore under the lectureId and PRN)
        db.collection("Subjects")
                .document(lectureId)
                .collection("Messages")
                .add(new Message(prn, message))
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(studentchat.this, "Feedback sent to teacher!", Toast.LENGTH_SHORT).show();
                    messageEditText.setText(""); // Clear message field
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(studentchat.this, "Error sending feedback", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchMessagesForStudent() {
        String prn = prnEditText.getText().toString().trim(); // Get PRN from student input

        if (prn.isEmpty()) {
            Toast.makeText(studentchat.this, "Please enter your PRN", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collectionGroup("Messages") // Fetch messages from any lecture's Messages collection
                .whereEqualTo("studentPrn", prn) // Only fetch messages with this PRN
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    messageListLayout.removeAllViews(); // Clear previous messages

                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(studentchat.this, "No feedback found", Toast.LENGTH_SHORT).show();
                    } else {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            String studentPrn = documentSnapshot.getString("studentPrn");
                            String messageContent = documentSnapshot.getString("messageContent");

                            if (studentPrn != null && messageContent != null) {
                                // Format and display the message
                                TextView messageTextView = new TextView(studentchat.this);
                                messageTextView.setText("PRN: " + studentPrn + "\nFeedback: " + messageContent);
                                messageTextView.setPadding(10, 10, 10, 10);
                                messageTextView.setTextSize(16);
                                messageListLayout.addView(messageTextView);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(studentchat.this, "Error fetching feedback", Toast.LENGTH_SHORT).show();
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