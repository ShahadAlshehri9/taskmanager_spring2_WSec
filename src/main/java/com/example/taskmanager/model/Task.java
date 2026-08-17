package com.example.taskmanager.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

// jakarta.persistence.* = the JPA annotations that map this class to a SQL table.
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

// jakarta.validation.* = the rules Spring checks on incoming JSON (see @Valid in the controller).
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
 * A single task. This is the base class; RecurringTask extends it,
 * which is where inheritance and polymorphism come in.
 *  @Entity tells JPA/Hibernate "make a database table out of this
 * class." Each Task object becomes one row. @Table just names that table.
 * every field 
 */
@Entity //This marks a class as a JPA entity
@Table(name = "tasks")//Specifies table mapping in the database we have a table created called tasks
public class Task {

    @Id //Defines primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)//lets the database
    // assign the id automatically (auto-increment)
    private Long id;                       // assigned by the database now (was: by the repository)
    // @NotBlank rejects null/empty/whitespace-only titles before saving.
    @NotBlank(message = "Task title must not be empty")//validations
    private String title;
    
    private String description;
    // @Enumerated(STRING) stores the enum by its name ("HIGH") instead of a
    // number, so the column stays readable in the database.
    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    private Priority priority;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column // not needed it's the default for any field in class labeled as @Entity in JPA
    private LocalDate dueDate; // may be null (no deadline)

    // @Column(updatable=false) means it's written once and never changed on updates.
    @Column(updatable = false)//Maps class field to table column
    private LocalDateTime createdAt;       // set once, never changes



    @ManyToOne(optional = false)      // many tasks -> one user
    @JoinColumn(name = "owner_id")    // adds an owner_id foreign-key column
    @JsonIgnore                       // never serialize the owner (and its hash) into JSON
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "project_id", nullable = true)
    private Project project;
    // JPA requires a no-argument constructor. but Hibernate/Jackson can use this.
    protected Task() {
        this.status = Status.TODO;
    }



    public Task(LocalDateTime createdAt, Long id, String title,
                String description, Priority priority, Status status,
                LocalDate dueDate, User owner, Project project) {
        this.createdAt = createdAt;
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.dueDate = dueDate;
        this.owner = owner;
        this.project=project;
    }

    // @PrePersist (below) fills it in right before insert.
    // Runs automatically right before this task is first saved to the database.
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    /* A task is overdue if it has a past due date and is not yet done. */
    public boolean isOverdue(LocalDate today) {
        return dueDate != null && status != Status.DONE && dueDate.isBefore(today);
    }

    /* Overridden by subclasses to add their own detail "polymorphism"  */
    public String describe() {
        return "%s [%s, %s]".formatted(title, priority, status);
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }



    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", dueDate=" + dueDate +
                ", createdAt=" + createdAt +
                ", owner=" + owner +
                '}';
    }
}
