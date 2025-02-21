package com.example.attendance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class LeaveAdapter extends RecyclerView.Adapter<LeaveAdapter.ViewHolder> {
    private List<LeaveRequest> leaveList;

    public LeaveAdapter(List<LeaveRequest> leaveList) {
        this.leaveList = leaveList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leave, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaveRequest leave = leaveList.get(position);
        holder.studentName.setText(leave.getStudentName());
        holder.status.setText(leave.getStatus());

        holder.approve.setOnClickListener(v -> updateLeaveStatus(leave.getRequestID(), "Approved", holder));
        holder.deny.setOnClickListener(v -> updateLeaveStatus(leave.getRequestID(), "Denied", holder));
    }

    private void updateLeaveStatus(String requestID, String status, ViewHolder holder) {
        FirebaseFirestore.getInstance().collection("sick_leave_requests")
                .document("teacher123").collection("requests").document(requestID)
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    holder.status.setText("Status: " + status);
                    Toast.makeText(holder.itemView.getContext(), "Status Updated", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return leaveList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView studentName, status;
        Button approve, deny;

        ViewHolder(View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.tvStudentName);
            status = itemView.findViewById(R.id.tvStatus);
            approve = itemView.findViewById(R.id.btnApprove);
            deny = itemView.findViewById(R.id.btnDeny);
}
    }
}
