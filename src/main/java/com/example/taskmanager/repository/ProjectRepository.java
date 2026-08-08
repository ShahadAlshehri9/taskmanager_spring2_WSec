package com.example.taskmanager.repository;

import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByLeader(User Leader);
    Optional<Project> findByIdAndLeader(Long id, User Leader);
    boolean existsByIdAndLeader(Long id, User Leader);
    void deleteByLeader(User Leader);
    Optional<Project> findByIdAndTeamMembers_Username(Long id, String username);
    List<Project> findByTeamMembersContains(User user);
}
