package com.java.labs.avi.dto;

public class ScheduleDto {
    private Long id;
    private String groupName;
    private String auditoriumNumber;
    private String subjectName;
    private String instructorName;
    private String dayOfWeek;
    private int numSubgroup;
    private int weekNumber;
    private String startTime;
    private String endTime;

    public ScheduleDto() {}

    public ScheduleDto(Long id, String groupName, String auditoriumNumber, String subjectName,
                       String instructorName, String dayOfWeek, int numSubgroup, int weekNumber,
                       String startTime, String endTime) {
        this.id = id;
        this.groupName = groupName;
        this.auditoriumNumber = auditoriumNumber;
        this.subjectName = subjectName;
        this.instructorName = instructorName;
        this.dayOfWeek = dayOfWeek;
        this.numSubgroup = numSubgroup;
        this.weekNumber = weekNumber;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public int getNumSubgroup() {
        return numSubgroup;
    }

    public void setNumSubgroup(int numSubgroup) {
        this.numSubgroup = numSubgroup;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
