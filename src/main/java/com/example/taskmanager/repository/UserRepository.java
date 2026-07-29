package com.example.taskmanager.repository;

import com.example.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);  // used at login
    //optional is generic container object used to safely represent a value that may or may not exist
    //It eliminates the need to return or pass null
    //preventing  NullPointerException errors by explicitly requiring the code to handle the absence of a value
    /* used to use these functions in security config ->
    isPresent(): Checks if a value exists.
    orElse(T other): Returns the value if present, otherwise returns a specified default value.
    orElseThrow(Consumer action lambda expression): Returns the value if present , otherwise executes a block of code only if the value not present
    ifPresent(Consumer action lambda expression): Executes a block of code only if the value is present.*/
    boolean existsByUsername(String username);       // used at sign-up the JPA will convert into SQL query WHERE username = ?
}