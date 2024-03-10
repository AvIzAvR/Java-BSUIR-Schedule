package com.java.labs.avi.service;

import com.java.labs.avi.dto.ScheduleDto;
import com.java.labs.avi.model.Auditorium;
import com.java.labs.avi.model.Group;
import com.java.labs.avi.model.Instructor;
import com.java.labs.avi.model.Schedule;
import com.java.labs.avi.model.Subject;
import com.java.labs.avi.repository.AuditoriumRepository;
import com.java.labs.avi.repository.GroupRepository;
import com.java.labs.avi.repository.InstructorRepository;
import com.java.labs.avi.repository.ScheduleRepository;
import com.java.labs.avi.repository.SubjectRepository;
import jakarta.transaction.Transactional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private InstructorRepository instructorRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private AuditoriumRepository auditoriumRepository;
    @Autowired
    private RestTemplate restTemplate;

    private static final String DEFAULT_VALUE = "не указано";

    public List<ScheduleDto> getScheduleByGroupDayWeekAndSubgroup(String groupNumber, String dayOfWeek, int targetWeekNumber, int numSubgroup) {
        String jsonResponse = fetchScheduleJson(groupNumber);
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            // Сначала проверяем, существует ли ключ "schedules" в полученном JSON
            if (jsonObject.has("schedules")) {
                JSONObject schedules = jsonObject.getJSONObject("schedules");
                // Проверяем, существует ли нужный день недели в "schedules"
                if (schedules.has(dayOfWeek)) {
                    JSONArray daySchedules = schedules.getJSONArray(dayOfWeek);
                    List<Schedule> processedSchedules = processSchedules(daySchedules, groupNumber, dayOfWeek, targetWeekNumber, numSubgroup);
                    return convertToDto(processedSchedules);
                }
            }
            return new ArrayList<>();
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }



    private String fetchScheduleJson(String groupNumber) {
        String url = "https://iis.bsuir.by/api/v1/schedule?studentGroup=" + groupNumber;
        return restTemplate.getForObject(url, String.class);
    }
    private JSONArray extractSchedulesFromJson(String jsonResponse, String dayOfWeek) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        return jsonObject.getJSONArray(dayOfWeek); // Убедитесь, что это правильный путь к данным
    }

    @Transactional
    protected List<Schedule> processSchedules(JSONArray schedulesJson, String groupNumber, String dayOfWeek, int targetWeekNumber, int numSubgroup) {
        List<Schedule> schedules = new ArrayList<>();
        for (int i = 0; i < schedulesJson.length(); i++) {
            try {
                JSONObject scheduleJson = schedulesJson.getJSONObject(i);
                Schedule schedule = processScheduleData(scheduleJson, groupNumber, dayOfWeek, targetWeekNumber, numSubgroup);
                schedules.add(schedule);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return schedules;
    }

    @Transactional
    private Schedule processScheduleData(JSONObject scheduleJson, String groupNumber, String dayOfWeek, int targetWeekNumber, int numSubgroup) throws JSONException {
        String subjectName = scheduleJson.optString("subjectFullName", DEFAULT_VALUE);
        String auditoryName = scheduleJson.optJSONArray("auditories").optString(0, DEFAULT_VALUE);
        String instructorFullName = extractInstructorFullName(scheduleJson);
        String startTime = scheduleJson.optString("startLessonTime", DEFAULT_VALUE);
        String endTime = scheduleJson.optString("endLessonTime", DEFAULT_VALUE);

        // Находим или создаем аудиторию
        Auditorium auditorium = auditoriumRepository.findByNumber(auditoryName)
                .orElseGet(() -> auditoriumRepository.save(new Auditorium(auditoryName)));

        // Находим или создаем группу, убедитесь, что устанавливаем связь с аудиторией
        Group group = groupRepository.findByName(groupNumber)
                .orElseGet(() -> {
                    Group newGroup = new Group(groupNumber);
                    newGroup.setAuditorium(auditorium); // Устанавливаем связь с аудиторией
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

        Schedule schedule = new Schedule();
        schedule.setGroup(group);
        schedule.setAuditorium(auditorium);
        schedule.setSubject(subject);
        schedule.setInstructor(instructor);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setNumSubgroup(numSubgroup);
        schedule.setWeekNumber(targetWeekNumber);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);

        return scheduleRepository.save(schedule);
    }


    private String extractInstructorFullName(JSONObject scheduleJson) throws JSONException {
        JSONArray employees = scheduleJson.optJSONArray("employees");
        if (employees != null && employees.length() > 0) {
            JSONObject instructorJson = employees.getJSONObject(0);
            String firstName = instructorJson.optString("firstName", "");
            String middleName = instructorJson.optString("middleName", "");
            String lastName = instructorJson.optString("lastName", "");
            return String.format("%s %s %s", firstName, middleName, lastName).trim();
        }
        return DEFAULT_VALUE;
    }

    public List<ScheduleDto> convertToDto(List<Schedule> schedules) {
        return schedules.stream().map(schedule -> new ScheduleDto(
                schedule.getId(),
                schedule.getGroup().getName(),
                schedule.getAuditorium().getNumber(),
                schedule.getSubject().getName(),
                schedule.getInstructor().getName(),
                schedule.getDayOfWeek(),
                schedule.getNumSubgroup(),
                schedule.getWeekNumber(),
                schedule.getStartTime(),
                schedule.getEndTime()
        )).collect(Collectors.toList());
    }
}
