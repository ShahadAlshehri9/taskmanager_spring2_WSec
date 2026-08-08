package com.example.taskmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name="projects")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Project {

    @Id //Defines primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)//lets the database assign the id automatically (auto-increment)
    private Long id;
    @NotBlank(message="The project title is required !")
    private String title;
    @ManyToOne(optional = false)      // many projects -> one user leader
    @JoinColumn(name = "leader_id")   // adds leader_id foreign-key column
    @JsonIgnore                       // never serialize the owner (and its hash) into JSON
    private User leader;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "project_team",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @JsonIgnore
    private Set<User> teamMembers = new HashSet<>();
    private String description;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column // not needed it's the default for any field in class labeled as @Entity in JPA
    private LocalDate dueDate; // may be null (no deadline)
    // @Column(updatable=false) means it's written once and never changed on updates.
    @Column(updatable = false)//Maps class field to table column set once, never changed
    private LocalDateTime createdAt;


    protected Project(){

    }

    public Project(String title, String description , LocalDate dueDate ,Status status) {
        this.title = title;
        this.description = description;
        this.dueDate= dueDate;
        this.status= status;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isOverdue(LocalDate today) {
        return dueDate != null && status != Status.DONE && dueDate.isBefore(today);
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", leader=" + leader +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", dueDate=" + dueDate +
                ", createdAt=" + createdAt +
                '}';
    }
}
