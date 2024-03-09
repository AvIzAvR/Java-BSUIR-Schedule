package com.java.labs.avi.repository;
import com.java.labs.avi.model.Group;
import com.java.labs.avi.model.Schedule;
import com.java.labs.avi.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository<LessonType> extends JpaRepository<Schedule, Long> {
    List<Schedule> findByGroupNumberAndDayOfWeekAndWeekNumberAndNumSubgroup(String groupNumber, String dayOfWeek, int weekNumber, int numSubgroup);
    Optional<Schedule> findByGroupAndDayOfWeekAndWeekNumberAndNumSubgroupAndSubject(
            Group group,
            String dayOfWeek,
            int weekNumber,
            int numSubgroup,
            Subject subject);
    }

