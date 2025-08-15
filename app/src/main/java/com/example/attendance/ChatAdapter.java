package com.example.attendance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    List<MessageModel> messageList;
    String currentUserID,subjectID,userRole;
    private Context context;
    public ChatAdapter(Context context,String subjectID,List<MessageModel> messageList, String currentUserID,String userRole) {
        this.context=context;
        this.subjectID=subjectID;
        this.messageList = messageList;
        this.currentUserID = currentUserID;
        this.userRole=userRole;
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).getSenderID().equals(currentUserID) ? 1 : 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == 1) ? R.layout.item_message_right : R.layout.item_message_left;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageModel message = messageList.get(position);
        holder.textMessage.setText(message.getSenderName() + ": " + message.getMessage());
        holder.itemView.setOnLongClickListener(v -> {
            MessageModel message1 = messageList.get(position);
            boolean isTeacher = userRole.equals("Teacher"); // Check if user is a teacher
            boolean isOwner = message1.getSenderID().equals(currentUserID); // Check if user sent the message

            if (isTeacher || isOwner) { // Teacher can delete any message, Student can delete own messages
                new AlertDialog.Builder(context)
                        .setTitle("Delete Message")
                        .setMessage("Are you sure you want to delete this message?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("chats")
                                    .document(subjectID)
                                    .collection("messages")
                                    .document(message1.getMessageID()) // Use message ID
                                    .delete()
                                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                Toast.makeText(context, "You can't delete this message", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

    }
    private void deleteMessage(String messageID, int position) {
        FirebaseFirestore.getInstance()
                .collection("chats")
                .document(subjectID) // Replace with actual subjectID
                .collection("messages")
                .document(messageID)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    messageList.remove(position);  // Remove from local list
                    notifyItemRemoved(position);  // Refresh RecyclerView
                    Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;

        public ViewHolder(View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
        }

    }
}