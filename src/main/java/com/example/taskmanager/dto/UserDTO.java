package com.example.taskmanager.dto;

import com.example.taskmanager.model.Role;
import com.example.taskmanager.model.User;

// Safe view of a User: id, username, role - never the password hash.
//A DTO allows you to cherry-pick only the specific field you need for a function or API request,
// rather than forcing the system to pass around the entire heavy object.
// A record = compact immutable(final classes) data carrier; this one line generates the
// constructor and the id()/username()/role() accessors.Setters/Getters
//With a record, Java automatically generates the constructor,
// private final fields, getters, equals(), hashCode(), and toString()
//A final class is a class that cannot be subclassed or extended through inheritance. for security
//Instead of returning raw database entities to the controller, we use this from() method to safely convert data on the fly
public record UserDTO(Long id, String username, Role role) {
    public static UserDTO from(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getRole());
    }
    //no passwords to view for the admin
}