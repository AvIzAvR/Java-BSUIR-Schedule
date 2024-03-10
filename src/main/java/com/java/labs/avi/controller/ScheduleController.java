package com.java.labs.avi.controller;

import com.java.labs.avi.dto.ScheduleDto;
import com.java.labs.avi.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        try {
            log.info("Fetching schedule for groupNumber: {}, dayOfWeek: {}, targetWeekNumber: {}, numSubgroup: {}", groupNumber, dayOfWeek, targetWeekNumber, numSubgroup);
            List<ScheduleDto> scheduleDtos = scheduleService.getScheduleByGroupDayWeekAndSubgroup(groupNumber, dayOfWeek, targetWeekNumber, numSubgroup);
            if(scheduleDtos.isEmpty()) {
                log.info("No schedules found for the given parameters.");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(scheduleDtos, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching schedule: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
