package com.java.labs.avi.service;

import com.java.labs.avi.model.ScheduleCache;
import com.java.labs.avi.dto.CourseInfoDto;
import com.java.labs.avi.dto.ScheduleDto;
import com.java.labs.avi.dto.ScheduleInfoDto;
import com.java.labs.avi.model.*;
import com.java.labs.avi.repository.*;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final SubjectRepository subjectRepository;
    private final InstructorRepository instructorRepository;
    private final GroupRepository groupRepository;
    private final AuditoriumRepository auditoriumRepository;
    private final RestTemplate restTemplate;
    private final ScheduleCache scheduleCache;
    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    public static final String notFound = "Schedule not found for this id :: ";
    private static final String DEFAULT_VALUE = "не указано";


    public ScheduleService(ScheduleRepository scheduleRepository, SubjectRepository subjectRepository,
                           InstructorRepository instructorRepository, GroupRepository groupRepository,
                           AuditoriumRepository auditoriumRepository, RestTemplate restTemplate, ScheduleCache scheduleCache) {
        this.scheduleRepository = scheduleRepository;
        this.subjectRepository = subjectRepository;
        this.instructorRepository = instructorRepository;
        this.groupRepository = groupRepository;
        this.auditoriumRepository = auditoriumRepository;
        this.restTemplate = restTemplate;
        this.scheduleCache = scheduleCache;
    }

    public List<ScheduleDto> getScheduleByGroupDayWeekAndSubgroup(String groupNumber, String dayOfWeek, int targetWeekNumber, int numSubgroup) {
        try {
            String jsonResponse = fetchScheduleJson(groupNumber);
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray daySchedules = jsonObject.getJSONObject("schedules").getJSONArray(dayOfWeek);
            List<Schedule> processedSchedules = processSchedules(daySchedules, groupNumber, dayOfWeek, targetWeekNumber, numSubgroup);
            return convertToDto(processedSchedules);
        } catch (Exception e) {
            logger.error("Error fetching schedule: ", e);
            return Collections.emptyList();
        }
    }

    private String fetchScheduleJson(String groupNumber) {
        String url = "https://iis.bsuir.by/api/v1/schedule?studentGroup=" + groupNumber;
        return restTemplate.getForObject(url, String.class);
    }

    protected List<Schedule> processSchedules(JSONArray schedulesJson, String groupNumber, String dayOfWeek, int targetWeekNumber, int numSubgroup) {
        List<Schedule> schedules = new ArrayList<>();
        logger.info("Start processing schedules for groupNumber: {}, dayOfWeek: {}, targetWeekNumber: {}, numSubgroup: {}", groupNumber, dayOfWeek, targetWeekNumber, numSubgroup);

        for (int i = 0; i < schedulesJson.length(); i++) {
            JSONObject scheduleJson = schedulesJson.getJSONObject(i);
            logger.info("Processing schedule JSON at index {}: {}", i, scheduleJson.toString());

            if (isValidForWeekAndSubgroup(scheduleJson, targetWeekNumber, numSubgroup)) {
                Schedule schedule = processScheduleData(scheduleJson, groupNumber, dayOfWeek, targetWeekNumber, numSubgroup);
                schedules.add(schedule);
                logger.info("Added schedule for " + ": {}", numSubgroup);
            } else {
                logger.info("Schedule at index {} does not match the criteria.", i);
            }
        }

        if (schedules.isEmpty()) {
            logger.warn("No schedules matched the criteria for groupNumber: {}, dayOfWeek: {}, targetWeekNumber: {}, numSubgroup: {}", groupNumber, dayOfWeek, targetWeekNumber, numSubgroup);
        } else {
            for (Schedule schedule : schedules) {
                ScheduleDto scheduleDto = convertToDto(Arrays.asList(schedule)).get(0);
                scheduleCache.put(scheduleDto.getId(), scheduleDto);
            }
            logger.info("Finished processing schedules. Total schedules added: {}", schedules.size());
        }

        return schedules;
    }

    protected boolean isValidForWeekAndSubgroup(JSONObject scheduleJson, int targetWeekNumber, int numSubgroup) {
        JSONArray weekNumbers = scheduleJson.optJSONArray("weekNumber");
        int subgroup = scheduleJson.optInt("numSubgroup", 0); // Устанавливаем значение по умолчанию равным 0

        boolean isWeekValid = weekNumbers != null && IntStream.range(0, weekNumbers.length()).anyMatch(i -> weekNumbers.getInt(i) == targetWeekNumber);
        boolean isSubgroupValid = (numSubgroup == 0 || subgroup == numSubgroup);

        logger.info("Checking validity for week: {}, subgroup: {}. Week numbers in JSON: {}, Subgroup in JSON: {}", targetWeekNumber, numSubgroup, Arrays.toString(IntStream.range(0, weekNumbers.length()).map(weekNumbers::getInt).toArray()), subgroup);
        logger.info("Week validity: {}, Subgroup validity: {}", isWeekValid, isSubgroupValid);

        return isWeekValid && isSubgroupValid;
    }





    protected Schedule processScheduleData(JSONObject scheduleJson, String groupNumber, String dayOfWeek, int targetWeekNumber, int numSubgroup) throws JSONException {
        String subjectName = scheduleJson.optString("subjectFullName", DEFAULT_VALUE);
        String auditoryName = scheduleJson.optJSONArray("auditories").optString(0, DEFAULT_VALUE);
        String instructorFullName = extractInstructorFullName(scheduleJson);
        String startTime = scheduleJson.optString("startLessonTime", DEFAULT_VALUE);
        String endTime = scheduleJson.optString("endLessonTime", DEFAULT_VALUE);
        int actualSubgroup = scheduleJson.optInt("numSubgroup", 0);

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
            newSchedule.setNumSubgroup(actualSubgroup);
            newSchedule.setWeekNumber(targetWeekNumber);
            newSchedule.setStartTime(startTime);
            newSchedule.setEndTime(endTime);

            Hibernate.initialize(newSchedule.getInstructor().getSubjects());
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
        return schedules.stream()
                .map(schedule -> {
                    CourseInfoDto courseInfo = new CourseInfoDto(
                            schedule.getGroup().getName(),
                            schedule.getAuditorium().getNumber(),
                            schedule.getSubject().getName(),
                            schedule.getInstructor().getName()
                    );
                    ScheduleInfoDto scheduleInfo = new ScheduleInfoDto(
                            schedule.getDayOfWeek(),
                            schedule.getNumSubgroup(), // Вот здесь уже есть информация о подгруппе
                            schedule.getWeekNumber(),
                            schedule.getStartTime(),
                            schedule.getEndTime()
                    );


                    return new ScheduleDto(schedule.getId(), courseInfo, scheduleInfo);
                })
                .collect(Collectors.toList());
    }


    public ScheduleDto createSchedule(Schedule schedule) {
        Schedule savedSchedule = scheduleRepository.save(schedule);
        ScheduleDto savedScheduleDto = convertToDto(Collections.singletonList(savedSchedule)).get(0);
        scheduleCache.put(savedSchedule.getId(), savedScheduleDto);
        return savedScheduleDto;
    }

    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    @Transactional
    public ScheduleDto updateSchedule(Long scheduleId, ScheduleDto scheduleDto) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException(notFound + scheduleId));

        Auditorium auditorium = auditoriumRepository
                .findByNumber(scheduleDto.getCourseInfo().getRoomNumber())
                .orElseGet(() -> auditoriumRepository.save(new Auditorium(scheduleDto.getCourseInfo().getRoomNumber())));

        Group group = groupRepository
                .findByName(scheduleDto.getCourseInfo().getClassGroup())
                .orElseGet(() -> {
                    Group newGroup = new Group(scheduleDto.getCourseInfo().getClassGroup());
                    newGroup.setAuditorium(auditorium);
                    return groupRepository.save(newGroup);
                });

        Subject subject = subjectRepository
                .findByName(scheduleDto.getCourseInfo().getCourseTitle())
                .orElseGet(() -> subjectRepository.save(new Subject(scheduleDto.getCourseInfo().getCourseTitle())));

        Instructor instructor = instructorRepository
                .findByName(scheduleDto.getCourseInfo().getLecturer())
                .orElseGet(() -> instructorRepository.save(new Instructor(scheduleDto.getCourseInfo().getLecturer())));

        schedule.setGroup(group);
        schedule.setAuditorium(auditorium);
        schedule.setSubject(subject);
        schedule.setInstructor(instructor);
        schedule.setDayOfWeek(scheduleDto.getScheduleInfo().getWeekday());
        schedule.setNumSubgroup(scheduleDto.getScheduleInfo().getSubgroupIndex());
        schedule.setWeekNumber(scheduleDto.getScheduleInfo().getWeekOrdinal());
        schedule.setStartTime(scheduleDto.getScheduleInfo().getSessionStart());
        schedule.setEndTime(scheduleDto.getScheduleInfo().getSessionEnd());

        Schedule updatedSchedule = scheduleRepository.save(schedule);

        CourseInfoDto courseInfoDto = new CourseInfoDto(
                updatedSchedule.getGroup().getName(),
                updatedSchedule.getAuditorium().getNumber(),
                updatedSchedule.getSubject().getName(),
                updatedSchedule.getInstructor().getName());

        ScheduleInfoDto scheduleInfoDto = new ScheduleInfoDto(
                updatedSchedule.getDayOfWeek(),
                updatedSchedule.getNumSubgroup(),
                updatedSchedule.getWeekNumber(),
                updatedSchedule.getStartTime(),
                updatedSchedule.getEndTime());

        return new ScheduleDto(updatedSchedule.getId(), courseInfoDto, scheduleInfoDto);
    }


    public void deleteSchedule(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(notFound + id));
        scheduleRepository.delete(schedule);
        scheduleCache.delete(id);
    }

    @Transactional
    public ScheduleDto patchSchedule(Long id, Map<String, Object> updates) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(notFound + id));

        updates.forEach((key, value) -> {
            if ("startTime".equals(key)) {
                schedule.setStartTime((String) value);
            } else if ("endTime".equals(key)) {
                schedule.setEndTime((String) value);
            }
        });

        Schedule updatedSchedules = scheduleRepository.save(schedule);
        ScheduleDto updatedScheduleDto = convertToDto(Collections.singletonList(updatedSchedules)).get(0);
        scheduleCache.put(updatedSchedules.getId(), updatedScheduleDto);
        return updatedScheduleDto;
    }

    public void deleteAuditorium(Long id) {
        List<Schedule> schedules = scheduleRepository.findByAuditoriumId(id);
        scheduleRepository.deleteAll(schedules);
        auditoriumRepository.deleteById(id);
    }


    public void deleteGroup(Long id) {
        groupRepository.deleteById(id);
    }

    public void deleteSubject(Long id) {
        subjectRepository.deleteById(id);
    }

    public ScheduleDto findById(Long id) {
        ScheduleDto scheduleDto = scheduleCache.get(id);
        if (scheduleDto == null) {
            Schedule schedule = scheduleRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Schedule not found for the id: " + id));
            scheduleDto = convertToDto(Collections.singletonList(schedule)).get(0);
            scheduleCache.put(id, scheduleDto);
        }
        return scheduleDto;
    }

    @Transactional
    public Map<Long, ScheduleDto> viewCache() {
        return scheduleCache.getCacheContents();
    }

    public Schedule convertToEntity(ScheduleDto scheduleDto) {
        Schedule schedule;
        if (scheduleDto.getId() != null) {
            schedule = scheduleRepository.findById(scheduleDto.getId())
                    .orElse(new Schedule());
        } else {
            schedule = new Schedule();
        }

        Auditorium auditorium = auditoriumRepository
                .findByNumber(scheduleDto.getCourseInfo().getRoomNumber())
                .orElseGet(() -> auditoriumRepository.save(new Auditorium(scheduleDto.getCourseInfo().getRoomNumber())));

        Group group = groupRepository
                .findByName(scheduleDto.getCourseInfo().getClassGroup())
                .orElseGet(() -> groupRepository.save(new Group(scheduleDto.getCourseInfo().getClassGroup())));

        Instructor instructor = instructorRepository
                .findByName(scheduleDto.getCourseInfo().getLecturer())
                .orElseGet(() -> instructorRepository.save(new Instructor(scheduleDto.getCourseInfo().getLecturer())));

        Subject subject = subjectRepository
                .findByName(scheduleDto.getCourseInfo().getCourseTitle())
                .orElseGet(() -> subjectRepository.save(new Subject(scheduleDto.getCourseInfo().getCourseTitle())));

        schedule.setAuditorium(auditorium);
        schedule.setGroup(group);
        schedule.setInstructor(instructor);
        schedule.setSubject(subject);

        ScheduleInfoDto scheduleInfo = scheduleDto.getScheduleInfo();
        if (scheduleInfo != null) {
            schedule.setDayOfWeek(scheduleInfo.getWeekday());
            schedule.setNumSubgroup(scheduleInfo.getSubgroupIndex());
            schedule.setWeekNumber(scheduleInfo.getWeekOrdinal());
            schedule.setStartTime(scheduleInfo.getSessionStart());
            schedule.setEndTime(scheduleInfo.getSessionEnd());
        }

        return schedule;
    }

}

