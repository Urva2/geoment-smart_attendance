package com.example.attendance;

public class Attendance {
    private String prn;
    private String name;
    private String attendanceStatus;

    public Attendance(String prn, String name, String attendanceStatus) {
        this.prn = prn;
        this.name = name;
        this.attendanceStatus = attendanceStatus;
    }

    public String getPrn() {
        return prn;
    }

    public String getName() {
        return name;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }
}