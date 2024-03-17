package com.java.labs.avi.model;

import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ScheduleCache {
    private final Map<Long, Schedule> cache = new ConcurrentHashMap<>();

    public Schedule get(Long id) {
        return cache.get(id);
    }

    public void put(Long id, Schedule schedule) {
        cache.put(id, schedule);
    }

    public void delete(Long id) {
        cache.remove(id);
    }

    public Map<Long, Schedule> getCacheContents() {
        return new HashMap<>(cache);    }
}
