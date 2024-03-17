package com.java.labs.avi.repository;

import com.java.labs.avi.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    @Query("SELECT s FROM Schedule s WHERE s.group.name = :groupName AND s.dayOfWeek = :dayOfWeek AND s.weekNumber = :weekNumber AND s.numSubgroup = :numSubgroup")
    List<Schedule> findByGroupNameAndDayOfWeekAndWeekNumberAndNumSubgroup(@Param("groupName") String groupName, @Param("dayOfWeek") String dayOfWeek, @Param("weekNumber") int weekNumber, @Param("numSubgroup") int numSubgroup);

    List<Schedule> findByGroupNameAndDayOfWeekAndWeekNumberAndNumSubgroupAndStartTimeAndEndTime(
            String groupName, String dayOfWeek, int weekNumber, int numSubgroup, String startTime, String endTime);
    List<Schedule> findByAuditoriumId(Long id);
}
