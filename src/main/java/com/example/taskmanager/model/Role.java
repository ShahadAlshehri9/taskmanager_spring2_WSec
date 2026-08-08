package com.example.taskmanager.model;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

//two types of accounts admin which can manage users only create/delete/list all users /find a user
//* a user account normal user --> use all his tasks services after auth
//Spring Security internally prefixes roles with ROLE_ so the hasRole("USER") will check ROLE_USER
public enum Role {
    ADMIN,USER
}