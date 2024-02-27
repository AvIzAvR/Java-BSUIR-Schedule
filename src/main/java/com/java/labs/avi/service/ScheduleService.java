package com.java.labs.avi.service;
import com.java.labs.avi.model.Schedule;
import com.java.labs.avi.repository.ScheduleRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private static final String DEFAULT_VALUE = "не указано";
    private static final String NUM_SUBGROUP = "numSubgroup";

    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }
    public List<Schedule> getScheduleByGroupDayWeekAndSubgroup(String groupNumber, String dayOfWeek, int targetWeekNumber, int numSubgroup) throws JSONException {
        if (targetWeekNumber > 4 || targetWeekNumber < 1) {
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
            String auditory = scheduleJson.optJSONArray("auditories").optString(0, DEFAULT_VALUE);
            String instructor = DEFAULT_VALUE;
            JSONArray employees = scheduleJson.optJSONArray("employees");
            if (employees != null && !employees.isEmpty()) {
                JSONObject instructorJson = employees.getJSONObject(0);
                String firstName = instructorJson.optString("firstName", "");
                String middleName = instructorJson.optString("middleName", "");
                String lastName = instructorJson.optString("lastName", "");
                instructor = String.format("%s %s %s", firstName, middleName, lastName).trim();
                instructor = instructor.isEmpty() ? DEFAULT_VALUE : instructor;
            }
            int weekNumber = 0;
            JSONArray weekNumbersArray = scheduleJson.optJSONArray("weekNumber");
            if (weekNumbersArray != null && weekNumbersArray.length() > 0) {
                for (int j = 0; j < weekNumbersArray.length(); j++) {
                    if (weekNumbersArray.getInt(j) == targetWeekNumber) {
                        weekNumber = targetWeekNumber;
                        break;
                    }
                }
            }
            if (weekNumber == 0) {
                continue;
            }
            int subgroupFromApi = scheduleJson.optInt(NUM_SUBGROUP, 0);
            if (numSubgroup != 0 && numSubgroup != subgroupFromApi) {
                continue;
            }
            List<Schedule> existingSchedules = scheduleRepository.findByGroupNumberAndDayOfWeekAndWeekNumberAndNumSubgroupAndSubjectAndLessonType(groupNumber, dayOfWeek, weekNumber, subgroupFromApi, subject, lessonType);
            if (existingSchedules.isEmpty()) {
                Schedule newSchedule = new Schedule();
                newSchedule.setGroupNumber(groupNumber);
                newSchedule.setDayOfWeek(dayOfWeek);
                newSchedule.setWeekNumber(weekNumber);
                newSchedule.setNumSubgroup(subgroupFromApi);
                newSchedule.setSubject(subject);
                newSchedule.setLessonType(lessonType);
                newSchedule.setAuditory(auditory);
                newSchedule.setInstructor(instructor);
                scheduleRepository.save(newSchedule);
            } else {
                Schedule existingSchedule = existingSchedules.get(0);
                existingSchedule.setSubject(subject);
                existingSchedule.setLessonType(lessonType);
                existingSchedule.setAuditory(auditory);
                existingSchedule.setInstructor(instructor);
                scheduleRepository.save(existingSchedule);
            }
        }
        if (numSubgroup != 0) {
            return scheduleRepository.findByGroupNumberAndDayOfWeekAndWeekNumberAndNumSubgroup(groupNumber, dayOfWeek, targetWeekNumber, numSubgroup);
        } else {
            return scheduleRepository.findByGroupNumberAndDayOfWeekAndWeekNumber(groupNumber, dayOfWeek, targetWeekNumber);
        }
    }

}

