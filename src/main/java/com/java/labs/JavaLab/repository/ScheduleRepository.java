package com.java.labs.JavaLab.repository;
import org.json.JSONArray;
import org.json.JSONObject;
import com.java.labs.JavaLab.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

}

