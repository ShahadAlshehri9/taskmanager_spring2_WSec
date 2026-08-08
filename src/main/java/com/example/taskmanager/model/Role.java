package com.example.taskmanager.model;
//two types of accounts admin which can manage users only create/delete/list all users /find a user
//* a user account normal user --> use all his tasks services after auth
//Spring Security internally prefixes roles with ROLE_ so the hasRole("USER") will check ROLE_USER
public enum Role {
    USER, ADMIN
}