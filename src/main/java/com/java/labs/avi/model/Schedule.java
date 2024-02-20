package com.java.labs.avi.model;

public class Schedule {
    private String subject;
    private String lessonType;
    private String auditory;
    private String instructor;

    private int numSubgroup;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getLessonType() {
        return lessonType;
    }

    public void setLessonType(String lessonType) {
        this.lessonType = lessonType;
    }

    public String getAuditory() {
        return auditory;
    }

    public void setAuditory(String auditory) {
        this.auditory = auditory;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public int getNumSubgroup() {
        return numSubgroup;
    }

    public void setNumSubgroup(int numSubgroup) {
        this.numSubgroup = numSubgroup;
    }
}
