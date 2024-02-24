package com.java.labs.avi.service;

import com.java.labs.avi.model.Schedule;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleService {

    public List<Schedule> getScheduleByGroupDayWeekAndSubgroup(String groupNumber, String dayOfWeek, int weekNumber, int numSubgroup) throws JSONException {
        String url = "https://iis.bsuir.by/api/v1/schedule?studentGroup=" + groupNumber;
        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.getForObject(url, String.class);
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray schedules = jsonObject.getJSONObject("schedules").getJSONArray(dayOfWeek);

        List<Schedule> scheduleList = new ArrayList<>();
        for (int i = 0; i < schedules.length(); i++) {
            JSONObject scheduleJson = schedules.getJSONObject(i);
            JSONArray weekNumbers = scheduleJson.getJSONArray("weekNumber");
            boolean isWeekMatched = weekNumbers.toList().contains(weekNumber);
            int scheduleSubgroup = scheduleJson.getInt("numSubgroup");
            if (isWeekMatched && (numSubgroup == 0 || scheduleSubgroup == numSubgroup)) {
                Schedule schedule = parseSchedule(scheduleJson);
                scheduleList.add(schedule);
            }
        }
        return scheduleList;
    }

    private Schedule parseSchedule(JSONObject scheduleJson) throws JSONException {
        Schedule schedule = new Schedule();
        schedule.setSubject(scheduleJson.getString("subjectFullName"));
        schedule.setLessonType(scheduleJson.getString("lessonTypeAbbrev"));
        schedule.setAuditory(scheduleJson.getJSONArray("auditories").getString(0));
        JSONObject instructorJson = scheduleJson.getJSONArray("employees").getJSONObject(0);
        String instructor = instructorJson.getString("firstName") + " " +
                instructorJson.optString("middleName", "") + " " +
                instructorJson.getString("lastName");
        schedule.setNumSubgroup(scheduleJson.getInt("numSubgroup"));

        return schedule;
    }
}
