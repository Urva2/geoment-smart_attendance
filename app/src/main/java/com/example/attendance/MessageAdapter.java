package com.example.attendance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<DocumentSnapshot> messageList; // Using Firestore DocumentSnapshot

    public MessageAdapter(List<DocumentSnapshot> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for the message
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_item, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        DocumentSnapshot message = messageList.get(position);

        // Assuming Firestore document contains fields: studentPrn and messageContent
        String studentPrn = message.getString("studentPrn"); // Getting student PRN
        String messageContent = message.getString("messageContent"); // Getting message content

        // Bind the data to the views
        holder.studentPrnTextView.setText("Student PRN: " + studentPrn);
        holder.messageContentTextView.setText(messageContent);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView studentPrnTextView;
        TextView messageContentTextView;

        public MessageViewHolder(View itemView) {
            super(itemView);

            studentPrnTextView = itemView.findViewById(R.id.studentPrnTextView);
            messageContentTextView = itemView.findViewById(R.id.messageContentTextView);
        }
    }
}