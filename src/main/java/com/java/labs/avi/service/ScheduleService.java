package com.java.labs.avi.service;
import com.java.labs.avi.repository.ScheduleRepository;
import com.java.labs.avi.model.Schedule;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }
    private static final String DEFAULT_VALUE = "не указано";
    private static final String NUM_SUBGROUP = "numSubgroup";

    public List<Schedule> getScheduleByGroupDayWeekAndSubgroup(String groupNumber, String dayOfWeek, int weekNumber, int numSubgroup) throws JSONException {
        if (weekNumber > 4 || weekNumber < 1) {
            throw new IllegalArgumentException("Неделя не может быть больше 4!");
        }
        if (numSubgroup < 0 || numSubgroup > 2) {
            throw new IllegalArgumentException("Подгруппы всего две!");
        }
        String url = "https://iis.bsuir.by/api/v1/schedule?studentGroup=" + groupNumber;
        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.getForObject(url, String.class);
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray schedules = jsonObject.getJSONObject("schedules").getJSONArray(dayOfWeek);
        for (int i = 0; i < schedules.length(); i++) {
            JSONObject scheduleJson = schedules.getJSONObject(i);
            String subject = scheduleJson.optString("subjectFullName", DEFAULT_VALUE);
            String lessonType = scheduleJson.optString("lessonTypeAbbrev", DEFAULT_VALUE);
            List<Schedule> existingSchedules = scheduleRepository.findByGroupNumberAndDayOfWeekAndWeekNumberAndNumSubgroupAndSubjectAndLessonType(groupNumber, dayOfWeek, weekNumber, numSubgroup, subject, lessonType);
            if (existingSchedules.isEmpty()) {
                Schedule newSchedule = parseSchedule(scheduleJson);
                newSchedule.setGroupNumber(groupNumber);
                newSchedule.setDayOfWeek(dayOfWeek);
                newSchedule.setWeekNumber(weekNumber);
                newSchedule.setNumSubgroup(numSubgroup);
                scheduleRepository.save(newSchedule);
            }
        }
        return scheduleRepository.findByGroupNumberAndDayOfWeekAndWeekNumberAndNumSubgroup(groupNumber, dayOfWeek, weekNumber, numSubgroup);
    }

    private Schedule parseSchedule(JSONObject scheduleJson) throws JSONException {
        Schedule schedule = new Schedule();
        schedule.setSubject(scheduleJson.optString("subjectFullName", DEFAULT_VALUE));
        schedule.setLessonType(scheduleJson.optString("lessonTypeAbbrev", DEFAULT_VALUE));
        JSONArray auditories = scheduleJson.optJSONArray("auditories");
        if (auditories != null && !auditories.isEmpty()) {
            schedule.setAuditory(auditories.optString(0, DEFAULT_VALUE));
        } else {
            schedule.setAuditory(DEFAULT_VALUE);
        }
        JSONArray employees = scheduleJson.optJSONArray("employees");
        if (employees != null && !employees.isEmpty()) {
            JSONObject instructorJson = employees.getJSONObject(0);
            String firstName = instructorJson.optString("firstName", "");
            String middleName = instructorJson.optString("middleName", "");
            String lastName = instructorJson.optString("lastName", "");
            String fullName = String.format("%s %s %s", firstName, middleName, lastName).trim();
            schedule.setInstructor(fullName.isEmpty() ? DEFAULT_VALUE : fullName);
        } else {
            schedule.setInstructor(DEFAULT_VALUE);
        }
        schedule.setNumSubgroup(scheduleJson.optInt(NUM_SUBGROUP, 0));
        return schedule;
    }
}