package com.example.attendance;

public class LeaveRequest {
    private String PRN;
    private String reason;
    private String startDate;
    private String endDate;
    private String documentUrl;
    private String status;
    private String studentBranch;
    private String studentYear;

    // Default constructor required for Firestore
    public LeaveRequest() { }

    public LeaveRequest(String PRN, String reason, String startDate, String endDate,
                        String documentUrl, String status, String studentBranch, String studentYear) {
        this.PRN = PRN;
        this.reason = reason;
        this.startDate = startDate;
        this.endDate = endDate;
        this.documentUrl = documentUrl;
        this.status = status;
        this.studentBranch = studentBranch;
        this.studentYear = studentYear;
    }

    public String getPRN() { return PRN; }
    public String getReason() { return reason; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getDocumentUrl() { return documentUrl; }
    public String getStatus() { return status; }
    public String getStudentBranch() { return studentBranch; }
    public String getStudentYear() { return studentYear; }

    public void setPRN(String PRN) { this.PRN = PRN; }
    public void setReason(String reason) { this.reason = reason; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }
    public void setStatus(String status) { this.status = status; }
    public void setStudentBranch(String studentBranch) { this.studentBranch = studentBranch; }
    public void setStudentYear(String studentYear) { this.studentYear = studentYear;}
}
