package com.example.attendance;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
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

public class LeaveRequestAdapter extends RecyclerView.Adapter<LeaveRequestAdapter.ViewHolder> {
    private List<LeaveRequest> leaveRequests;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "TeacherPrefs";
    private static final String KEY_BRANCH = "branch";
    private static final String KEY_YEAR = "year";

    public LeaveRequestAdapter(Context context, List<LeaveRequest> leaveRequests) {
        this.context = context;
        this.leaveRequests = leaveRequests;
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtStudentInfo, txtReason, txtStatus;
        Button btnViewDoc, btnApprove, btnDeny;

        public ViewHolder(View view) {
            super(view);
            txtStudentInfo = view.findViewById(R.id.txtStudentInfo);
            txtReason = view.findViewById(R.id.txtReason);
            txtStatus = view.findViewById(R.id.txtStatus);
            btnViewDoc = view.findViewById(R.id.btnViewDoc);
            btnApprove = view.findViewById(R.id.btnApprove);
            btnDeny = view.findViewById(R.id.btnDeny);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leave_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaveRequest request = leaveRequests.get(position);
        holder.txtStudentInfo.setText("PRN: " + request.getPRN());
        holder.txtReason.setText("Reason: " + request.getReason());
        holder.txtStatus.setText("Status: " + request.getStatus());

       // holder.btnViewDoc.setOnClickListener(v -> {
         //   Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getDocumentUrl()));
           // context.startActivity(browserIntent);
        //});
        holder.btnViewDoc.setOnClickListener(v -> {
            String pdfUrl = request.getDocumentUrl(); // Ensure this is the correct field

            if (pdfUrl != null && !pdfUrl.isEmpty()) {
                try {
                    // Open in WebView using Google Drive (safer option)
                    String driveViewerUrl = "https://drive.google.com/viewerng/viewer?embedded=true&url=" + pdfUrl;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(driveViewerUrl));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    v.getContext().startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(v.getContext(), "Error opening document", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(v.getContext(), "No document found", Toast.LENGTH_SHORT).show();
}
        });

        holder.btnApprove.setOnClickListener(v -> updateLeaveStatus(request.getPRN(), "Approved"));
        holder.btnDeny.setOnClickListener(v -> updateLeaveStatus(request.getPRN(), "Denied"));
    }

    @Override
    public int getItemCount() {
        return leaveRequests.size();
    }

    private void updateLeaveStatus(String prn, String status) {
        // Retrieve branch and year from SharedPreferences
        String branch = sharedPreferences.getString(KEY_BRANCH, "");
        String year = sharedPreferences.getString(KEY_YEAR, "");

        if (branch.isEmpty() || year.isEmpty()) {
            Toast.makeText(context, "Branch and Year not set!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("leave_requests").document(branch).collection(year)
                .document(prn).update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore Update", "Status Updated: " + prn);
                    Toast.makeText(context, "Status updated", Toast.LENGTH_SHORT).show();
                    for (LeaveRequest request : leaveRequests) {
                        if (request.getPRN().equals(prn)) {
                            request.setStatus(status); // Update local list
                            break;
                        }
                    }
                    notifyDataSetChanged(); // Refresh RecyclerView
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Update", "Status not Updated: " + e);
                    Toast.makeText(context, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
           });
}
}
