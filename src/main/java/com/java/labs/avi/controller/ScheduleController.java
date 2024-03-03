package com.java.labs.avi.controller;
import com.java.labs.avi.model.Schedule;
import com.java.labs.avi.service.ScheduleService;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {
    private final ScheduleService scheduleService;
    @Autowired
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }
    @GetMapping
    public ResponseEntity<List<Schedule>> getScheduleForDayOfWeek(
            @RequestParam String groupNumber,
            @RequestParam String dayOfWeek,
            @RequestParam int targetWeekNumber,
            @RequestParam int numSubgroup) throws JSONException {

        List<Schedule> scheduleList = scheduleService.getScheduleByGroupDayWeekAndSubgroup(groupNumber, dayOfWeek, targetWeekNumber, numSubgroup);
        return new ResponseEntity<>(scheduleList, HttpStatus.OK);
    }
    @PostMapping
    public ResponseEntity<Schedule> createSchedule(@RequestBody Schedule schedule) {
        Schedule savedSchedule = scheduleService.saveSchedule(schedule);
        return new ResponseEntity<>(savedSchedule, HttpStatus.CREATED);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Schedule> updateSchedule(@PathVariable Long id, @RequestBody Schedule schedule) {
        Schedule updatedSchedule = scheduleService.updateSchedule(id, schedule);
        if (updatedSchedule == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(updatedSchedule, HttpStatus.OK);
    }
    @PatchMapping("/{id}")
    public ResponseEntity<Schedule> patchSchedule(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Schedule patchedSchedule = scheduleService.patchSchedule(id, updates);
        if (patchedSchedule == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(patchedSchedule, HttpStatus.OK);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {
        boolean isDeleted = scheduleService.deleteSchedule(id);
        if (isDeleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}