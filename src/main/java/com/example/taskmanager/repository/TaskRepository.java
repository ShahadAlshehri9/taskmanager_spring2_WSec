package com.example.taskmanager.repository;

import com.example.taskmanager.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/*
 *
 * SPRING NOTE: by extending JpaRepository<Task, Long> you inherit save(),
 * findById(), findAll(), deleteById(), existsById(), count() and more - Spring
 * generates the actual database code at runtime. You write NO implementation.
 * The Persistence Layer interacts with the Database Layer using Spring Data JPA or R2DBC,
 * often through a Repository Class(interface) that extends CRUD services JpaRepository<>
 * The two methods below are "derived queries": Spring reads the method NAME and
 * writes the matching SQL for you (findByStatus -> WHERE status = ?). They are
 * the database-native alternative to filtering with streams in the service.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByOwner(User owner);
    Optional<Task> findByIdAndOwner(Long id, User owner);
    boolean existsByIdAndOwner(Long id, User owner);
    void deleteByOwner(User owner);
    List<Task> findByProjectAndOwner(Project project, User owner);
}
