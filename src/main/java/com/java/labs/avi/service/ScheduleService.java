package com.java.labs.avi.service;
import com.java.labs.avi.model.Schedule;
import com.java.labs.avi.repository.ScheduleRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private static final String DEFAULT_VALUE = "не указано";
    private static final String NUM_SUBGROUP = "numSubgroup";

    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }
    public List<Schedule> getScheduleByGroupDayWeekAndSubgroup(String groupNumber, String dayOfWeek, int targetWeekNumber, int numSubgroup) throws JSONException {
        validateWeekNumber(targetWeekNumber);
        validateNumSubgroup(numSubgroup);

        String jsonResponse = fetchScheduleJson(groupNumber);
        JSONArray schedules = extractSchedulesFromJson(jsonResponse, dayOfWeek);

        processSchedules(schedules, groupNumber, dayOfWeek, targetWeekNumber, numSubgroup);

        return retrieveSchedules(groupNumber, dayOfWeek, targetWeekNumber, numSubgroup);
    }
    private void validateWeekNumber(int targetWeekNumber) {
        if (targetWeekNumber > 4 || targetWeekNumber < 1) {
            throw new IllegalArgumentException("Неделя не может быть больше 4!");
        }
    }
    private void validateNumSubgroup(int numSubgroup) {
        if (numSubgroup < 0 || numSubgroup > 2) {
            throw new IllegalArgumentException("Подгруппы всего две!");
        }
    }
    private String fetchScheduleJson(String groupNumber) {
        String url = "https://iis.bsuir.by/api/v1/schedule?studentGroup=" + groupNumber;
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, String.class);
    }
    private JSONArray extractSchedulesFromJson(String jsonResponse, String dayOfWeek) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        return jsonObject.getJSONObject("schedules").getJSONArray(dayOfWeek);
    }
    private void processSchedules(JSONArray schedules, String groupNumber, String dayOfWeek, int targetWeekNumber, int numSubgroup) throws JSONException {
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
            int weekNumber = findWeekNumber(scheduleJson.getJSONArray("weekNumber"), targetWeekNumber);
            if (weekNumber == 0 || (numSubgroup != 0 && numSubgroup != scheduleJson.optInt(NUM_SUBGROUP, -1))) {
                continue;
            }
            String startTime = scheduleJson.optString("startLessonTime", DEFAULT_VALUE);
            String endTime = scheduleJson.optString("endLessonTime", DEFAULT_VALUE);
            Schedule schedule = new Schedule();
            schedule.setGroupNumber(groupNumber);
            schedule.setDayOfWeek(dayOfWeek);
            schedule.setWeekNumber(weekNumber);
            schedule.setNumSubgroup(numSubgroup);
            schedule.setSubject(subject);
            schedule.setLessonType(lessonType);
            schedule.setAuditory(auditory);
            schedule.setInstructor(instructor);
            schedule.setStartTime(startTime);
            schedule.setEndTime(endTime);
            saveOrUpdateSchedule(schedule);
        }
    }
    private int findWeekNumber(JSONArray weekNumbersArray, int targetWeekNumber) throws JSONException {
        for (int j = 0; j < weekNumbersArray.length(); j++) {
            if (weekNumbersArray.getInt(j) == targetWeekNumber) {
                return targetWeekNumber;
            }
        }
        return 0;
    }
    private void saveOrUpdateSchedule(Schedule schedule) {
        List<Schedule> existingSchedules = scheduleRepository.findByGroupNumberAndDayOfWeekAndWeekNumberAndNumSubgroupAndSubjectAndLessonType(
                schedule.getGroupNumber(),
                schedule.getDayOfWeek(),
                schedule.getWeekNumber(),
                schedule.getNumSubgroup(),
                schedule.getSubject(),
                schedule.getLessonType()
        );
        if (!existingSchedules.isEmpty()) {
            Schedule existingSchedule = existingSchedules.get(0);
            schedule.setId(existingSchedule.getId());
        }
        scheduleRepository.save(schedule);
    }
    private List<Schedule> retrieveSchedules(String groupNumber, String dayOfWeek, int targetWeekNumber, int numSubgroup) {
        if (numSubgroup != 0) {
            return scheduleRepository.findByGroupNumberAndDayOfWeekAndWeekNumberAndNumSubgroup(
                    groupNumber, dayOfWeek, targetWeekNumber, numSubgroup);
        } else {
            List<Schedule> schedulesForAllSubgroups = new ArrayList<>();
            schedulesForAllSubgroups.addAll(scheduleRepository.findByGroupNumberAndDayOfWeekAndWeekNumberAndNumSubgroup(
                    groupNumber, dayOfWeek, targetWeekNumber, numSubgroup));
            schedulesForAllSubgroups.addAll(scheduleRepository.findByGroupNumberAndDayOfWeekAndWeekNumberAndNumSubgroup(
                    groupNumber, dayOfWeek, targetWeekNumber, 1));
            schedulesForAllSubgroups.addAll(scheduleRepository.findByGroupNumberAndDayOfWeekAndWeekNumberAndNumSubgroup(
                    groupNumber, dayOfWeek, targetWeekNumber, 2));

            return schedulesForAllSubgroups;
        }
    }
    public Schedule saveSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }
    public Schedule updateSchedule(Long id, Schedule updatedSchedule) {
        return scheduleRepository.findById(id).map(schedule -> {
            schedule.setSubject(updatedSchedule.getSubject());
            schedule.setLessonType(updatedSchedule.getLessonType());
            schedule.setAuditory(updatedSchedule.getAuditory());
            schedule.setInstructor(updatedSchedule.getInstructor());
            schedule.setGroupNumber(updatedSchedule.getGroupNumber());
            schedule.setDayOfWeek(updatedSchedule.getDayOfWeek());
            schedule.setNumSubgroup(updatedSchedule.getNumSubgroup());
            schedule.setWeekNumber(updatedSchedule.getWeekNumber());
            schedule.setStartTime(updatedSchedule.getStartTime());
            schedule.setEndTime(updatedSchedule.getEndTime());
            return scheduleRepository.save(schedule);
        }).orElse(null);
    }
    public Schedule patchSchedule(Long id, Map<String, Object> updates) {
        return scheduleRepository.findById(id).map(schedule -> {
            updates.forEach((key, value) -> {
                switch (key) {
                    case "subject": schedule.setSubject((String) value); break;
                    case "lessonType": schedule.setLessonType((String) value); break;
                    case "auditory": schedule.setAuditory((String) value); break;
                    case "instructor": schedule.setInstructor((String) value); break;
                    case "groupNumber": schedule.setGroupNumber((String) value); break;
                    case "dayOfWeek": schedule.setDayOfWeek((String) value); break;
                    case "numSubgroup": schedule.setNumSubgroup((int) value); break;
                    case "weekNumber": schedule.setWeekNumber((int) value); break;
                    case "startTime": schedule.setStartTime((String) value); break;
                    case "endTime": schedule.setEndTime((String) value); break;
                }
            });
            return scheduleRepository.save(schedule);
        }).orElse(null);
    }
    public boolean deleteSchedule(Long id) {
        return scheduleRepository.findById(id).map(schedule -> {
            scheduleRepository.delete(schedule);
            return true;
        }).orElse(false);
    }

}