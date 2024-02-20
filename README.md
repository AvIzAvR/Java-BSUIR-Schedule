# Java Lab Schedule Project

This project is designed to manage and retrieve schedules for students, focusing on daily and weekly schedules. It interfaces with an external API to fetch schedule data, then parses and presents this information based on group numbers, days of the week, week numbers, and subgroup numbers. The application is built with Spring Boot and utilizes RestTemplate for API communication.

## Project Structure

The project is organized into several key packages, each serving a specific purpose within the application:

- **com.java.labs.JavaLab.config:** Contains configuration classes, including `RestTemplateConfig` for setting up `RestTemplate`.
- **com.java.labs.JavaLab.controller:** Houses the `ScheduleController`, which manages API endpoints for retrieving schedule information.
- **com.java.labs.JavaLab.json:** Includes `ScheduleParser` for parsing schedule data fetched from external APIs.
- **com.java.labs.JavaLab.model:** Contains the `Schedule` model representing the schedule information.
- **com.java.labs.JavaLab.service:** Comprises `ScheduleService` that contains the logic to fetch and process schedule data.
- **com.java.labs.JavaLab:** Contains the `JavaLabApplication`, the entry point of the Spring Boot application.

## Dependencies

- **Spring Boot:** Framework for building the application.
- **Spring Web:** For creating RESTful services.
- **RestTemplate:** For making HTTP requests to external APIs.
- **JSON.org:** For parsing JSON data returned from external APIs.

## How to Run the Project

1. Clone the repository: `git clone https://github.com/your-username/JavaLab.git`
2. Navigate to the project directory: `cd JavaLab`
3. Build the project: `mvn clean install`
4. Run the application: `mvn spring-boot:run`

After starting the application, it will be accessible for API requests.

## API Endpoints

- **GET /schedule/{groupNumber}/{dayOfWeek}/{weekNumber}/{numSubgroup}:** Retrieves the schedule for a specific group, day, week, and subgroup.

## Sample Usage

```bash
# Retrieve schedule for a specific day, week, and subgroup
curl "http://localhost:8080/schedule/1234/Monday/1/1"
