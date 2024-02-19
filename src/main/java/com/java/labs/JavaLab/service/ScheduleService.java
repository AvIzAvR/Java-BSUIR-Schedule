package com.java.labs.JavaLab.service;

import com.java.labs.JavaLab.model.Schedule;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import java.util.List;

import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

import org.json.JSONArray;


@Service
public class ScheduleService {

    public List<Schedule> getScheduleByGroupDayWeekAndSubgroup(String groupNumber, String dayOfWeek, int weekNumber, int numSubgroup) {
        String url = "https://iis.bsuir.by/api/v1/schedule?studentGroup=" + groupNumber;
        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.getForObject(url, String.class);
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray schedules = jsonObject.getJSONObject("schedules").getJSONArray(dayOfWeek);

        List<Schedule> scheduleList = new ArrayList<>();
        for (int i = 0; i < schedules.length(); i++) {
            JSONObject scheduleJson = schedules.getJSONObject(i);
            JSONArray weekNumbers = scheduleJson.getJSONArray("weekNumber");
            if (weekNumbers.toList().contains(weekNumber) && scheduleJson.getInt("numSubgroup") == numSubgroup) {
                Schedule schedule = new Schedule();
                schedule.setSubject(scheduleJson.getString("subjectFullName"));
                schedule.setLessonType(scheduleJson.getString("lessonTypeAbbrev"));
                schedule.setAuditory(scheduleJson.getJSONArray("auditories").getString(0));
                JSONObject instructorJson = scheduleJson.getJSONArray("employees").getJSONObject(0);
                String instructor = instructorJson.getString("firstName") + " " +
                        instructorJson.getString("middleName") + " " +
                        instructorJson.getString("lastName");
                schedule.setInstructor(instructor);
                schedule.setNumSubgroup(numSubgroup);
                scheduleList.add(schedule);
            }
        }
        return scheduleList;
    }
}