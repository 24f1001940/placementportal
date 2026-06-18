package com.saqib.placementportal.controller;

import com.saqib.placementportal.dto.ApiDtos.BlacklistRequest;
import com.saqib.placementportal.dto.ApiDtos.CompanyResponse;
import com.saqib.placementportal.dto.ApiDtos.DashboardStats;
import com.saqib.placementportal.dto.ApiDtos.DriveResponse;
import com.saqib.placementportal.dto.ApiDtos.PageResponse;
import com.saqib.placementportal.dto.ApiDtos.UserResponse;
import com.saqib.placementportal.entity.RoleName;
import com.saqib.placementportal.service.AdminService;
import com.saqib.placementportal.service.CompanyService;
import com.saqib.placementportal.service.DriveService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;
    private final CompanyService companyService;
    private final DriveService driveService;

    public AdminController(AdminService adminService, CompanyService companyService, DriveService driveService) {
        this.adminService = adminService;
        this.companyService = companyService;
        this.driveService = driveService;
    }

    @GetMapping("/stats")
    public DashboardStats stats() {
        return adminService.stats();
    }

    @GetMapping("/users")
    public PageResponse<UserResponse> users(@RequestParam(required = false) String query,
                                            @RequestParam(required = false) RoleName role,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return adminService.users(query, role, page, size);
    }

    @PatchMapping("/users/{id}/blacklist")
    public UserResponse blacklist(@PathVariable Long id, @RequestBody BlacklistRequest request) {
        return adminService.blacklist(id, request.blacklisted());
    }

    @GetMapping("/companies/pending")
    public PageResponse<CompanyResponse> pendingCompanies(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        return companyService.pending(page, size);
    }

    @GetMapping("/drives/pending")
    public PageResponse<DriveResponse> pendingDrives(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return driveService.pending(page, size);
    }

    @GetMapping("/reports/applications.csv")
    public ResponseEntity<byte[]> applicationsCsv() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("placement-applications.csv").build().toString())
                .body(adminService.applicationsCsv());
    }
}
