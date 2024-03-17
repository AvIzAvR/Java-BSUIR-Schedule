package com.java.labs.avi.controller;

import com.java.labs.avi.model.Schedule;
import com.java.labs.avi.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CacheController {

    private final ScheduleService scheduleService;

    @Autowired
    public CacheController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/cache/schedules")
    public ResponseEntity<Map<Long, Schedule>> getCacheContents() {
        Map<Long, Schedule> cacheContents = scheduleService.viewCache();
        return ResponseEntity.ok(cacheContents);
    }
}
