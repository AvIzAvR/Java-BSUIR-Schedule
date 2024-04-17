package com.java.labs.avi.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class RequestCounterService {
    private final AtomicLong requestCount = new AtomicLong();

    public void incrementCount() {
        requestCount.incrementAndGet();
    }

    public long getRequestCount() {
        return requestCount.get();
    }
}
