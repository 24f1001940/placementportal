package com.saqib.placementportal.controller;

import com.saqib.placementportal.dto.ApiDtos.DriveRequest;
import com.saqib.placementportal.dto.ApiDtos.DriveResponse;
import com.saqib.placementportal.dto.ApiDtos.PageResponse;
import com.saqib.placementportal.dto.ApiDtos.StatusUpdateRequest;
import com.saqib.placementportal.entity.DriveStatus;
import com.saqib.placementportal.service.DriveService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/drives")
public class DriveController {
    private final DriveService driveService;

    public DriveController(DriveService driveService) {
        this.driveService = driveService;
    }

    @GetMapping
    public PageResponse<DriveResponse> approved(@RequestParam(required = false) String query,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "12") int size,
                                                @RequestParam(defaultValue = "-createdAt") String sort) {
        return driveService.approved(query, page, size, sort);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('COMPANY')")
    public DriveResponse create(@Valid @RequestBody DriveRequest request) {
        return driveService.create(request);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('COMPANY')")
    public List<DriveResponse> mine() {
        return driveService.myCompanyDrives();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<DriveResponse> pending(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return driveService.pending(page, size);
    }

    @GetMapping("/{id}")
    public DriveResponse get(@PathVariable Long id) {
        return driveService.get(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public DriveResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return driveService.updateStatus(id, DriveStatus.valueOf(request.status().toUpperCase()));
    }
}
