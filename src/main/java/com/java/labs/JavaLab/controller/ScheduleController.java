package com.java.labs.JavaLab.controller;

import com.java.labs.JavaLab.model.Schedule;
import com.java.labs.JavaLab.service.ScheduleService;
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
    @GetMapping("/{groupNumber}/{dayOfWeek}/{weekNumber}/{numSubgroup}")
    public ResponseEntity<List<Schedule>> getScheduleForDayOfWeek(
            @PathVariable String groupNumber,
            @PathVariable String dayOfWeek,
            @PathVariable int weekNumber,
            @PathVariable int numSubgroup) throws JSONException {

        List<Schedule> scheduleList = scheduleService.getScheduleByGroupDayWeekAndSubgroup(groupNumber, dayOfWeek, weekNumber, numSubgroup);
        return new ResponseEntity<>(scheduleList, HttpStatus.OK);
    }
}