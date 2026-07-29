package com.example.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/*
 * @SpringBootTest starts the whole application context. This test passes if the
 * app can wire everything up and connect to the database. (It needs the database
 * running - see README - otherwise skip it with `mvn package -DskipTests`.)
 */
@SpringBootTest
class TaskManagerApplicationTests {

    @Test
    void contextLoads() {
    }
}
