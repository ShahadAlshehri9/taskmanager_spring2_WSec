package com.example.taskmanager.repository;

import com.example.taskmanager.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findTop20ByUsernameOrderByCreatedAtDesc(String username);

}
