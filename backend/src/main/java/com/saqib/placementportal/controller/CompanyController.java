package com.saqib.placementportal.controller;

import com.saqib.placementportal.dto.ApiDtos.CompanyProfileRequest;
import com.saqib.placementportal.dto.ApiDtos.CompanyResponse;
import com.saqib.placementportal.dto.ApiDtos.PageResponse;
import com.saqib.placementportal.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('COMPANY')")
    public CompanyResponse me() {
        return companyService.myProfile();
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('COMPANY')")
    public CompanyResponse updateMe(@Valid @RequestBody CompanyProfileRequest request) {
        return companyService.updateMyProfile(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<CompanyResponse> search(@RequestParam(required = false) String query,
                                                @RequestParam(required = false) Boolean approved,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return companyService.search(query, approved, page, size);
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public CompanyResponse approve(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean approved) {
        return companyService.approve(id, approved);
    }
}
