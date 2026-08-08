package com.example.taskmanager.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class Authorities {
    public static List<GrantedAuthority> forRole(Role role) {
        List<String> names = new ArrayList<>();
        names.add("ROLE_" + role.name());

        switch (role) {
            case ADMIN -> names.addAll(List.of(
                    "USER_VIEW", "PROJECT_VIEW_ALL", "PROJECT_TASK_VIEW_ALL",
                    "PROJECT_PROGRESS_VIEW_ALL", "LEADER_ASSIGN", "LEADER_REVOKE","DELETE_USER"));
            case USER -> names.addAll(List.of(
                    "PROJECT_VIEW_ASSIGNED", "PROJECT_MEMBER_ADD", "PROJECT_MEMBER_REMOVE",
                    "PROJECT_TASK_CREATE", "PROJECT_TASK_UPDATE", "PROJECT_TASK_DELETE", "PROJECT_TASK_ASSIGN", "PROJECT_PROGRESS_VIEW","PROJECT_VIEW_MEMBER",
                    "PROJECT_PROGRESS_VIEW_MEMBER", "PROJECT_TASK_VIEW_ASSIGNED", "PERSONAL_TASK_CREATE",
                    "PERSONAL_TASK_UPDATE", "PERSONAL_TASK_DELETE"));
        }
        return names.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}