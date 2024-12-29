package com.example.attendance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private List<Attendance> attendanceList;

    public AttendanceAdapter(List<Attendance> attendanceList) {
        this.attendanceList = attendanceList;
    }

    @Override
    public AttendanceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_item, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AttendanceViewHolder holder, int position) {
        Attendance attendance = attendanceList.get(position);
        holder.prnTextView.setText(attendance.getPrn());
        holder.nameTextView.setText(attendance.getName());
        holder.attendanceStatusTextView.setText(attendance.getAttendanceStatus());
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView prnTextView, nameTextView, attendanceStatusTextView;

        public AttendanceViewHolder(View itemView) {
            super(itemView);
            prnTextView = itemView.findViewById(R.id.prnTextView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            attendanceStatusTextView = itemView.findViewById(R.id.attendanceStatusTextView);
        }
    }
}