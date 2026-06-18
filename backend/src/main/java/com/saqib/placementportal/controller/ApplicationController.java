package com.saqib.placementportal.controller;

import com.saqib.placementportal.dto.ApiDtos.ApplicationRequest;
import com.saqib.placementportal.dto.ApiDtos.ApplicationResponse;
import com.saqib.placementportal.dto.ApiDtos.StatusUpdateRequest;
import com.saqib.placementportal.entity.ApplicationStatus;
import com.saqib.placementportal.service.ApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('STUDENT')")
    public ApplicationResponse apply(@Valid @RequestBody ApplicationRequest request) {
        return applicationService.apply(request);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public List<ApplicationResponse> mine() {
        return applicationService.myApplications();
    }

    @GetMapping("/company")
    @PreAuthorize("hasRole('COMPANY')")
    public List<ApplicationResponse> companyApplications() {
        return applicationService.companyApplications();
    }

    @GetMapping("/drive/{driveId}")
    @PreAuthorize("hasAnyRole('ADMIN','COMPANY')")
    public List<ApplicationResponse> driveApplications(@PathVariable Long driveId) {
        return applicationService.driveApplications(driveId);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','COMPANY')")
    public ApplicationResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return applicationService.updateStatus(id, ApplicationStatus.valueOf(request.status().toUpperCase()));
    }
}
