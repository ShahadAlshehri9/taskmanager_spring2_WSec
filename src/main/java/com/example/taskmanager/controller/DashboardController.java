package com.example.taskmanager.controller;


import com.example.taskmanager.model.Activity;
import com.example.taskmanager.service.ActivityService;
import com.example.taskmanager.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;
    private final ActivityService activityService;

    protected DashboardController(DashboardService dashboardService,ActivityService activityService) {
        this.dashboardService = dashboardService;
        this.activityService=activityService;
    }


    @GetMapping("/activities")
    @PreAuthorize("isAuthenticated()")
    public List<Activity> myRecentActivity(Principal principal) {
        return activityService.recentForUser(principal.getName(), 20);
    }

    @GetMapping("/tasks/progress")
    @PreAuthorize("isAuthenticated()")
    public int myProgress(Principal p) {
        return dashboardService.myProgress(p.getName()); }

    @GetMapping("/projects/{id}/progress")
    @PreAuthorize("isAuthenticated()")   // getById inside enforces leadership?
    public int projectProgress(@PathVariable Long id, Principal p) {
        return dashboardService.projectProgress(id, p.getName());
    }

}
