package com.example.attendance;

public class LeaveRequest {
    private String requestID, studentName, PRN, branch, year, startDate, endDate, reason, medicalCertificateUrl, status;

    public LeaveRequest() { }

    public String getRequestID() { return requestID; }
    public void setRequestID(String requestID) { this.requestID = requestID; }

    public String getStudentName() { return studentName; }
    public String getStatus() { return status;}
}