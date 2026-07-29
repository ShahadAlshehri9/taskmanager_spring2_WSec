package com.example.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * @SpringBootApplication switches on component scanning (Spring finds
 * @RestController, @Service, @Repository, etc.) and auto-configuration
 * (it wires up Tomcat, the database connection, Hibernate, and so on).
 * Running main() starts the web server; it then stays up waiting for requests.
 */
@SpringBootApplication // This annotation is used to mark the main class of a Spring Boot application.
public class TaskManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskManagerApplication.class, args);
    }
}
