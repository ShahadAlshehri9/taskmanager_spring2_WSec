package com.example.taskmanager.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class Authorities {
    private static final List<String> VIEW_EVERYTHING = List.of(
            "USER_VIEW",                  // list all user accounts
            "PROJECT_VIEW_ALL",           // list every project
            "PROJECT_TASK_VIEW_ALL",      // see the tasks of any project
            "PROJECT_PROGRESS_VIEW_ALL",  // see the progress of any project
            "TEAM_VIEW"                   // list any project's team members
    );
    public static List<GrantedAuthority> forRole(Role role) {
        List<String> names = new ArrayList<>();
        names.add("ROLE_" + role.name());

        switch (role) {
            case ADMIN -> {
                names.addAll(VIEW_EVERYTHING);
                names.add("USER_DELETE");
                names.add("USER_ROLE_SET");
            }
            case MANAGER -> {
                names.addAll(VIEW_EVERYTHING);
                names.addAll(List.of(
                        "PROJECT_CREATE", "PROJECT_UPDATE", "PROJECT_DELETE",
                        "LEADER_ASSIGN", "LEADER_REVOKE","PERSONAL_TASK_CREATE", "PERSONAL_TASK_UPDATE", "PERSONAL_TASK_DELETE",
                        "PROJECT_VIEW_MEMBER", "PROJECT_PROGRESS_VIEW_MEMBER", "PROJECT_TASK_VIEW_ASSIGNED"
                        ));
            }
            case USER -> names.addAll(List.of(
                    // personal tasks
                    "PERSONAL_TASK_CREATE", "PERSONAL_TASK_UPDATE", "PERSONAL_TASK_DELETE",
                    // as a project member
                    "PROJECT_VIEW_MEMBER", "PROJECT_PROGRESS_VIEW_MEMBER", "PROJECT_TASK_VIEW_ASSIGNED",
                    // as a project leader -- ADDITIONALLY restricted to the led
                    // project by ProjectSecurity.isLeader(...)
                    "PROJECT_MEMBER_ADD", "PROJECT_MEMBER_REMOVE",
                    "PROJECT_TASK_CREATE", "PROJECT_TASK_UPDATE",
                    "PROJECT_TASK_DELETE", "PROJECT_TASK_ASSIGN"));
        }
        return names.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}