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

        private ScheduleDto(Builder builder) {
            this.id = builder.id;
            this.groupName = builder.groupName;
            this.auditoriumNumber = builder.auditoriumNumber;
            this.subjectName = builder.subjectName;
            this.instructorName = builder.instructorName;
            this.dayOfWeek = builder.dayOfWeek;
            this.numSubgroup = builder.numSubgroup;
            this.weekNumber = builder.weekNumber;
            this.startTime = builder.startTime;
            this.endTime = builder.endTime;
        }

        public static class Builder {
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

            public Builder() {
            }

            public Builder setId(Long id) {
                this.id = id;
                return this;
            }

            public Builder setGroupName(String groupName) {
                this.groupName = groupName;
                return this;
            }

            public Builder setAuditoriumNumber(String auditoriumNumber) {
                this.auditoriumNumber = auditoriumNumber;
                return this;
            }

            public Builder setSubjectName(String subjectName) {
                this.subjectName = subjectName;
                return this;
            }

            public Builder setInstructorName(String instructorName) {
                this.instructorName = instructorName;
                return this;
            }

            public Builder setDayOfWeek(String dayOfWeek) {
                this.dayOfWeek = dayOfWeek;
                return this;
            }

            public Builder setNumSubgroup(int numSubgroup) {
                this.numSubgroup = numSubgroup;
                return this;
            }

            public Builder setWeekNumber(int weekNumber) {
                this.weekNumber = weekNumber;
                return this;
            }

            public Builder setStartTime(String startTime) {
                this.startTime = startTime;
                return this;
            }

            public Builder setEndTime(String endTime) {
                this.endTime = endTime;
                return this;
            }

            public ScheduleDto build() {
                return new ScheduleDto(this);
            }
        }

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