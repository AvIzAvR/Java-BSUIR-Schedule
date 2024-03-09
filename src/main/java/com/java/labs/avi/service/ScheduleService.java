package com.java.labs.avi.service;
import com.java.labs.avi.model.Schedule;
import com.java.labs.avi.model.Subject;
import com.java.labs.avi.model.Instructor;
import com.java.labs.avi.model.Group;
import com.java.labs.avi.model.Auditorium;
import com.java.labs.avi.repository.ScheduleRepository;
import com.java.labs.avi.repository.SubjectRepository;
import com.java.labs.avi.repository.InstructorRepository;
import com.java.labs.avi.repository.GroupRepository;
import com.java.labs.avi.repository.AuditoriumRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final SubjectRepository subjectRepository;
    private final InstructorRepository instructorRepository;
    private final GroupRepository groupRepository;
    private final AuditoriumRepository auditoriumRepository;
    private static final String DEFAULT_VALUE = "не указано";

    @Autowired
    public ScheduleService(ScheduleRepository scheduleRepository, SubjectRepository subjectRepository,
                           InstructorRepository instructorRepository, GroupRepository groupRepository,
                           AuditoriumRepository auditoriumRepository) {
        this.scheduleRepository = scheduleRepository;
        this.subjectRepository = subjectRepository;
        this.instructorRepository = instructorRepository;
        this.groupRepository = groupRepository;
        this.auditoriumRepository = auditoriumRepository;
    }

    public List<Schedule> getScheduleByGroupDayWeekAndSubgroup(String groupNumber, String dayOfWeek, int targetWeekNumber, int numSubgroup) throws JSONException {
        String jsonResponse = fetchScheduleJson(groupNumber);
        JSONArray schedules = extractSchedulesFromJson(jsonResponse, dayOfWeek);
        processSchedules(schedules, groupNumber, dayOfWeek, targetWeekNumber, numSubgroup);
        return retrieveSchedules(groupNumber, dayOfWeek, targetWeekNumber, numSubgroup);
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

    private void processSchedules(JSONArray schedulesJson, String groupNumber, String dayOfWeek, int targetWeekNumber, int numSubgroup) throws JSONException {
        List<Schedule> schedules = new ArrayList<>();
        for (int i = 0; i < schedulesJson.length(); i++) {
            JSONObject scheduleJson = schedulesJson.getJSONObject(i);
            String subjectName = scheduleJson.optString("subjectFullName", DEFAULT_VALUE);
            String lessonType = scheduleJson.optString("lessonTypeAbbrev", DEFAULT_VALUE);
            String auditoryName = scheduleJson.optJSONArray("auditories").optString(0, DEFAULT_VALUE);
            String instructorFullName = extractInstructorFullName(scheduleJson);

            Subject subject = subjectRepository.findByName(subjectName)
                    .orElseGet(() -> subjectRepository.save(new Subject(subjectName)));

            Instructor instructor = instructorRepository.findByFullName(instructorFullName)
                    .orElseGet(() -> instructorRepository.save(new Instructor(instructorFullName)));

            Group group = groupRepository.findByName(groupNumber)
                    .orElseGet(() -> groupRepository.save(new Group(groupNumber)));

            Auditorium auditorium = auditoriumRepository.findByNumber(auditoryName)
                    .orElseGet(() -> auditoriumRepository.save(new Auditorium(auditoryName)));

            Schedule schedule = new Schedule();
            schedule.setSubject(subject);
            schedule.setInstructor(instructor);
            schedule.setGroup(group);
            schedule.setAuditorium(auditorium);
            schedule.setDayOfWeek(dayOfWeek);
            schedule.setWeekNumber(targetWeekNumber);
            schedule.setNumSubgroup(numSubgroup);
            schedule.setStartTime(scheduleJson.optString("startLessonTime", DEFAULT_VALUE));
            schedule.setEndTime(scheduleJson.optString("endLessonTime", DEFAULT_VALUE));
            schedules.add(schedule);
        }
        scheduleRepository.saveAll(schedules);
    }

    private String extractInstructorFullName(JSONObject scheduleJson) throws JSONException {
        JSONArray employees = scheduleJson.optJSONArray("employees");
        if (employees != null && !employees.isEmpty()) {
            JSONObject instructorJson = employees.getJSONObject(0);
            String firstName = instructorJson.optString("firstName", "");
            String middleName = instructorJson.optString("middleName", "");
            String lastName = instructorJson.optString("lastName", "");
            return String.format("%s %s %s", firstName, middleName, lastName).trim();
        }
        return DEFAULT_VALUE;
    }


    private int findWeekNumber(JSONArray weekNumbersArray, int targetWeekNumber) throws JSONException {
        for (int j = 0; j < weekNumbersArray.length(); j++) {
            if (weekNumbersArray.getInt(j) == targetWeekNumber) {
                return targetWeekNumber;
            }
        }
        return 0;
    }

    private void saveOrUpdateSchedule(Schedule newSchedule) {
        Optional<Schedule> existingScheduleOpt = scheduleRepository
                .findByGroupAndDayOfWeekAndWeekNumberAndNumSubgroupAndSubject(
                        newSchedule.getGroup(),
                        newSchedule.getDayOfWeek(),
                        newSchedule.getWeekNumber(),
                        newSchedule.getNumSubgroup(),
                        newSchedule.getSubject());
        if (existingScheduleOpt.isPresent()) {
            Schedule existingSchedule = existingScheduleOpt.get();
            updateExistingSchedule(existingSchedule, newSchedule);
            scheduleRepository.save(existingSchedule);
        } else {
            scheduleRepository.save(newSchedule);
        }
    }
    private void updateExistingSchedule(Schedule existingSchedule, Schedule newSchedule) {
        existingSchedule.setDayOfWeek(newSchedule.getDayOfWeek());
        existingSchedule.setWeekNumber(newSchedule.getWeekNumber());
        existingSchedule.setNumSubgroup(newSchedule.getNumSubgroup());
        existingSchedule.setStartTime(newSchedule.getStartTime());
        existingSchedule.setEndTime(newSchedule.getEndTime());
        existingSchedule.setSubject(newSchedule.getSubject());
        existingSchedule.setInstructor(newSchedule.getInstructor());
        existingSchedule.setGroup(newSchedule.getGroup());
        existingSchedule.setAuditorium(newSchedule.getAuditorium());
    }


    private List<Schedule> retrieveSchedules(String groupNumber, String dayOfWeek, int targetWeekNumber, int numSubgroup) {
        if (numSubgroup != 0) {
            return scheduleRepository.findByGroupNumberAndDayOfWeekAndWeekNumberAndNumSubgroup(
                    groupNumber, dayOfWeek, targetWeekNumber, numSubgroup);
        } else {
            List<Schedule> schedulesForAllSubgroups = new ArrayList<>();
            schedulesForAllSubgroups.addAll(scheduleRepository.findByGroupNumberAndDayOfWeekAndWeekNumberAndNumSubgroup(
                    groupNumber, dayOfWeek, targetWeekNumber, 0));
            schedulesForAllSubgroups.addAll(scheduleRepository.findByGroupNumberAndDayOfWeekAndWeekNumberAndNumSubgroup(
                    groupNumber, dayOfWeek, targetWeekNumber, 1));
            schedulesForAllSubgroups.addAll(scheduleRepository.findByGroupNumberAndDayOfWeekAndWeekNumberAndNumSubgroup(
                    groupNumber, dayOfWeek, targetWeekNumber, 2));
            return schedulesForAllSubgroups;
        }
    }
}
