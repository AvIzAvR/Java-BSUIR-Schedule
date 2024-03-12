package com.java.labs.avi.dto;

public class CourseInfoDto {
    private String groupName;
    private String auditoriumNumber;
    private String subjectName;
    private String instructorName;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getAuditoriumNumber() {
        return auditoriumNumber;
    }

    public void setAuditoriumNumber(String auditoriumNumber) {
        this.auditoriumNumber = auditoriumNumber;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public CourseInfoDto(String groupName, String auditoriumNumber, String subjectName, String instructorName) {
        this.groupName = groupName;
        this.auditoriumNumber = auditoriumNumber;
        this.subjectName = subjectName;
        this.instructorName = instructorName;
    }
}
