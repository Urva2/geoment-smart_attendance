package com.example.attendance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    List<MessageModel> messageList;
    String currentUserID;

    public ChatAdapter(List<MessageModel> messageList, String currentUserID) {
        this.messageList = messageList;
        this.currentUserID = currentUserID;
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
