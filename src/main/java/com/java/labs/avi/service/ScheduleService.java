package com.java.labs.avi.service;

import com.java.labs.avi.dto.ScheduleDto;
import com.java.labs.avi.model.*;
import com.java.labs.avi.repository.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final SubjectRepository subjectRepository;
    private final InstructorRepository instructorRepository;
    private final GroupRepository groupRepository;
    private final AuditoriumRepository auditoriumRepository;
    private final RestTemplate restTemplate;

    public ScheduleService(ScheduleRepository scheduleRepository, SubjectRepository subjectRepository,
                           InstructorRepository instructorRepository, GroupRepository groupRepository,
                           AuditoriumRepository auditoriumRepository, RestTemplate restTemplate) {
        this.scheduleRepository = scheduleRepository;
        this.subjectRepository = subjectRepository;
        this.instructorRepository = instructorRepository;
        this.groupRepository = groupRepository;
        this.auditoriumRepository = auditoriumRepository;
        this.restTemplate = restTemplate;
    }


    private static final String DEFAULT_VALUE = "не указано";

    public List<ScheduleDto> getScheduleByGroupDayWeekAndSubgroup(String groupNumber, String dayOfWeek, int targetWeekNumber, int numSubgroup) {
        String jsonResponse = fetchScheduleJson(groupNumber);
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray daySchedules = jsonObject.getJSONObject("schedules").getJSONArray(dayOfWeek);
        List<Schedule> processedSchedules = processSchedules(daySchedules, groupNumber, dayOfWeek, targetWeekNumber, numSubgroup);
        return convertToDto(new ArrayList<>(new HashSet<>(processedSchedules))); // Ensure uniqueness
    }

    private String fetchScheduleJson(String groupNumber) {
        String url = "https://iis.bsuir.by/api/v1/schedule?studentGroup=" + groupNumber;
        return restTemplate.getForObject(url, String.class);
    }

    protected List<Schedule> processSchedules(JSONArray schedulesJson, String groupNumber, String dayOfWeek, int targetWeekNumber, int numSubgroup) {
        List<Schedule> schedules = new ArrayList<>();
        for (int i = 0; i < schedulesJson.length(); i++) {
            JSONObject scheduleJson = schedulesJson.getJSONObject(i);
            Schedule schedule = processScheduleData(scheduleJson, groupNumber, dayOfWeek, targetWeekNumber, numSubgroup);
            schedules.add(schedule);
        }
        return schedules;
    }

    protected Schedule processScheduleData(JSONObject scheduleJson, String groupNumber, String dayOfWeek, int targetWeekNumber, int numSubgroup) throws JSONException {
        String subjectName = scheduleJson.optString("subjectFullName", DEFAULT_VALUE);
        String auditoryName = scheduleJson.optJSONArray("auditories").optString(0, DEFAULT_VALUE);
        String instructorFullName = extractInstructorFullName(scheduleJson);
        String startTime = scheduleJson.optString("startLessonTime", DEFAULT_VALUE);
        String endTime = scheduleJson.optString("endLessonTime", DEFAULT_VALUE);

        Auditorium auditorium = auditoriumRepository.findByNumber(auditoryName)
                .orElseGet(() -> auditoriumRepository.save(new Auditorium(auditoryName)));

        Group group = groupRepository.findByName(groupNumber)
                .orElseGet(() -> {
                    Group newGroup = new Group(groupNumber);
                    newGroup.setAuditorium(auditorium);
                    return groupRepository.save(newGroup);
                });

        Instructor instructor = instructorRepository.findByName(instructorFullName)
                .orElseGet(() -> instructorRepository.save(new Instructor(instructorFullName)));

        Subject subject = subjectRepository.findByName(subjectName)
                .orElseGet(() -> {
                    Subject newSubject = new Subject(subjectName);
                    newSubject.getInstructors().add(instructor);
                    return subjectRepository.save(newSubject);
                });

        if (!subject.getInstructors().contains(instructor)) {
            subject.getInstructors().add(instructor);
            subjectRepository.save(subject);
        }

        List<Schedule> existingSchedules = scheduleRepository.findByGroupNameAndDayOfWeekAndWeekNumberAndNumSubgroupAndStartTimeAndEndTime(
                groupNumber, dayOfWeek, targetWeekNumber, numSubgroup, startTime, endTime);

        if (!existingSchedules.isEmpty()) {
            return existingSchedules.get(0);
        } else {
            Schedule newSchedule = new Schedule();
            newSchedule.setGroup(group);
            newSchedule.setAuditorium(auditorium);
            newSchedule.setSubject(subject);
            newSchedule.setInstructor(instructor);
            newSchedule.setDayOfWeek(dayOfWeek);
            newSchedule.setNumSubgroup(numSubgroup);
            newSchedule.setWeekNumber(targetWeekNumber);
            newSchedule.setStartTime(startTime);
            newSchedule.setEndTime(endTime);

            return scheduleRepository.save(newSchedule);
        }
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

    public List<ScheduleDto> convertToDto(List<Schedule> schedules) {
        return schedules.stream().distinct().map(schedule ->
                new ScheduleDto.Builder()
                        .setId(schedule.getId())
                        .setGroupName(schedule.getGroup().getName())
                        .setAuditoriumNumber(schedule.getAuditorium().getNumber())
                        .setSubjectName(schedule.getSubject().getName())
                        .setInstructorName(schedule.getInstructor().getName())
                        .setDayOfWeek(schedule.getDayOfWeek())
                        .setNumSubgroup(schedule.getNumSubgroup())
                        .setWeekNumber(schedule.getWeekNumber())
                        .setStartTime(schedule.getStartTime())
                        .setEndTime(schedule.getEndTime())
                        .build()
        ).toList();
    }
}
