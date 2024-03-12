package com.java.labs.avi.dto;

public class ScheduleInfoDto {
    private String dayOfWeek;
    private int numSubgroup;
    private int weekNumber;
    private String startTime;
    private String endTime;

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

    public ScheduleInfoDto(String dayOfWeek, int numSubgroup, int weekNumber, String startTime, String endTime) {
        this.dayOfWeek = dayOfWeek;
        this.numSubgroup = numSubgroup;
        this.weekNumber = weekNumber;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
