package com.example.taskmanager.controller;

import com.example.taskmanager.model.Status;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/*
 This is the presentation layer - the part of the app that
 presents itself to other programs over HTTP
 menu: instead of printing "1) List tasks" and reading keystrokes,
 each method (GET/POST/PUT/DELETE) answers one HTTP request
 All the real logic lives in TaskService.

 SECURITY: every method takes a Principal - Spring Security injects it after
 authentication. principal.getName() = the logged-in username, which the service
 uses to limit every operation to that user's own tasks.
 */

@RestController//tells Spring that this class will handle REST API requests.
/*any class annotated with @RestController can processe incoming HTTP requests and returns data objects rather than views.*/
@RequestMapping("/tasks")//It helps define URL path for handling HTTP requests
public class TaskController {

    private final TaskService service;

    // Same DI idea as the service: Spring passes the bean in.
    public TaskController(TaskService service) {
        this.service = service;
    }

    // GET /tasks  -> list every task THIS user owns
    @GetMapping //Retrieves data from the server all data in this case
    public List<Task> getAll(Principal principal) {
        return service.getAll(principal.getName());
    }

    // GET /tasks/5  -> one task by id. {id} in the URL becomes the method
    // parameter via @PathVariable Extracts values from the URL path and binds them to method parameters.
    @GetMapping("/{id}")//Retrieves data from the server # needs info for specification
    public Task getById(@PathVariable Long id, Principal principal) {
        return service.getById(id, principal.getName());
    }

    // POST /tasks  -> create a task     (menu option 2)
    // @RequestBody turns the incoming JSON into a Task object Binds the HTTP request body to a Java object. Commonly used with POST and PUT requests.
    // @Valid runs the @NotBlank String must contain at least one non-whitespace character / @NotNull Field value must not be null checks first validate request . Returns HTTP 201 Created.success
    @PostMapping//Sends data to the server # no info is needed but should send a body in the http request as JSON
    public ResponseEntity<Task> create(@Valid @RequestBody Task task, Principal principal) {
        Task saved = service.add(task, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT /tasks/5  -> replace/edit an existing task
    @PutMapping("/{id}")//Updates existing data with one info.
    public Task update(@PathVariable Long id, @Valid @RequestBody Task task, Principal principal) {
        return service.update(id, task, principal.getName());
    }

    // PATCH /tasks/5/status?value=DONE  -> change just the status (option 3)
    // @RequestParam Reads query parameters from the request URL. Used for optional or filtering inputs.
    @PatchMapping("/{id}/status")//Updates existing data with much info for more specification
    public Task changeStatus(@PathVariable Long id, @RequestParam("value") Status value, Principal principal) {
        return service.changeStatus(id, value, principal.getName());
    }

    // DELETE /tasks/5  -> remove a task  (menu option 4). 204 = success, no body.
    @DeleteMapping("/{id}")//Deletes data # needs info !
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        service.delete(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    // GET /tasks/search?keyword=String  (menu option 5)
    @GetMapping("/search")
    public List<Task> search(@RequestParam String keyword, Principal principal) {
        return service.search(keyword, principal.getName());
    }
    //combine all the search function
    // GET /tasks/status/DONE
    @GetMapping("/status/{status}")
    public List<Task> byStatus(@PathVariable Status status, Principal principal) {
        return service.byStatus(status, principal.getName());
    }

    // GET /api/tasks/overdue  (menu option 6)
    @GetMapping("/overdue")
    public List<Task> overdue(Principal principal) {
        return service.overdue(LocalDate.now(), principal.getName());
    }

    // GET /api/tasks/stats  -> counts by status  (menu option 7)
    @GetMapping("/stats")
    public Map<Status, Long> stats(Principal principal) {
        return service.countByStatus(principal.getName());
    }
}