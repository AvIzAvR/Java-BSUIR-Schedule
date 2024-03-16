package com.java.labs.avi.controller;

import com.java.labs.avi.dto.ScheduleDto;
import com.java.labs.avi.model.Schedule;
import com.java.labs.avi.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    private static final Logger log = LoggerFactory.getLogger(ScheduleController.class);
    private final ScheduleService scheduleService;

    @Autowired
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public ResponseEntity<List<ScheduleDto>> getScheduleForDayOfWeek(@RequestParam String groupNumber,
                                                                     @RequestParam String dayOfWeek,
                                                                     @RequestParam int targetWeekNumber,
                                                                     @RequestParam int numSubgroup) {
        List<ScheduleDto> scheduleDtos = scheduleService.getScheduleByGroupDayWeekAndSubgroup(groupNumber, dayOfWeek, targetWeekNumber, numSubgroup);
        if (scheduleDtos.isEmpty()) {
            log.info("No schedules found for the given parameters.");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(scheduleDtos, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Schedule> createSchedule(@RequestBody Schedule schedule) {
        return new ResponseEntity<>(scheduleService.createSchedule(schedule), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleDto> updateSchedule(@PathVariable Long id, @RequestBody ScheduleDto scheduleDetails) {
        ScheduleDto updatedScheduleDto = scheduleService.updateSchedule(id, scheduleDetails);
        return ResponseEntity.ok(updatedScheduleDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ScheduleDto> patchSchedule(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        ScheduleDto updatedScheduleDto = scheduleService.patchSchedule(id, updates);
        return ResponseEntity.ok(updatedScheduleDto);
    }

    @DeleteMapping("/auditorium/{id}")
    public ResponseEntity<HttpStatus> deleteAuditorium(@PathVariable Long id) {
        scheduleService.deleteAuditorium(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/group/{id}")
    public ResponseEntity<HttpStatus> deleteGroup(@PathVariable Long id) {
        scheduleService.deleteGroup(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/subject/{id}")
    public ResponseEntity<HttpStatus> deleteSubject(@PathVariable Long id) {
        scheduleService.deleteSubject(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
