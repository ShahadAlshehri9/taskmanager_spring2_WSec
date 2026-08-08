package com.example.taskmanager.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)  // so no two accounts share a name
    private String username;

    @Size(min= 6, message = "Password must be 6 characters at least")
    @Column(nullable = false)// in the database level this will not allow database to store null in any row by mistake
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)   // accepted on input, never serialized
    private String password; //The password column stores a hash
    // store "USER"/"ADMIN" as readable text
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    protected User() {}   // JPA needs a no-arg constructor

    public User(String username, String password, Role role) {
        this.password = password;
        this.username = username;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public  Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}