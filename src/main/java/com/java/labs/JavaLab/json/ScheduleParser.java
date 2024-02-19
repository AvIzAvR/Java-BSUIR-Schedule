package com.java.labs.JavaLab.json;

import com.java.labs.JavaLab.model.Schedule;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ScheduleParser {

    public static List<Schedule> parseScheduleForDay(JSONObject scheduleJson, String dayOfWeek) throws JSONException {
        List<Schedule> schedulesForDay = new ArrayList<>();

        // Получаем расписание для указанного дня недели из JSON объекта
        JSONArray daySchedule = scheduleJson.getJSONObject("schedules").optJSONArray(dayOfWeek);

        // Проверяем, есть ли расписание для указанного дня
        if (daySchedule != null) {
            // Проходимся по каждому элементу расписания для данного дня
            for (int i = 0; i < daySchedule.length(); i++) {
                JSONObject lessonJson = daySchedule.getJSONObject(i);
                Schedule schedule = parseScheduleItem(lessonJson);
                schedulesForDay.add(schedule);
            }
        }

        return schedulesForDay;
    }

    private static Schedule parseScheduleItem(JSONObject lessonJson) throws JSONException {
        // Извлекаем нужные поля из JSON объекта
        String subject = lessonJson.getString("subjectFullName");
        String lessonType = lessonJson.getString("lessonTypeAbbrev");
        String auditory = lessonJson.getJSONArray("auditories").getString(0);
        String instructor = lessonJson.getJSONArray("employees").getJSONObject(0).getString("firstName") + " " +
                lessonJson.getJSONArray("employees").getJSONObject(0).getString("lastName");

        // Создаем объект Schedule
        Schedule schedule = new Schedule();
        schedule.setSubject(subject);
        schedule.setLessonType(lessonType);
        schedule.setAuditory(auditory);
        schedule.setInstructor(instructor);

        return schedule;
    }
}
