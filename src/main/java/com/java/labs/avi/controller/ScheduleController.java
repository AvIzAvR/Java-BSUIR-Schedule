package com.java.labs.avi.controller;
import com.java.labs.avi.model.Schedule;
import com.java.labs.avi.service.ScheduleService;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
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
}