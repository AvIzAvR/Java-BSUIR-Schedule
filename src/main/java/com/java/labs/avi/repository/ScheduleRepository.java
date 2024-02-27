package com.java.labs.avi.repository;

import com.java.labs.avi.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByGroupNumberAndDayOfWeekAndWeekNumberAndNumSubgroup(String groupNumber, String dayOfWeek, int weekNumber, int numSubgroup);
    List<Schedule> findByGroupNumberAndDayOfWeekAndWeekNumberAndNumSubgroupAndSubjectAndLessonType(String groupNumber, String dayOfWeek, int weekNumber, int numSubgroup, String subject, String lessonType);
    List<Schedule> findByGroupNumberAndDayOfWeekAndWeekNumber(String groupNumber, String dayOfWeek, int targetWeekNumber);
}
