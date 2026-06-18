package com.saqib.placementportal.service;

import com.saqib.placementportal.dto.ApiDtos.CompanyProfileRequest;
import com.saqib.placementportal.dto.ApiDtos.CompanyResponse;
import com.saqib.placementportal.dto.ApiDtos.PageResponse;
import com.saqib.placementportal.entity.Company;
import com.saqib.placementportal.entity.RoleName;
import com.saqib.placementportal.entity.UserAccount;
import com.saqib.placementportal.exception.ForbiddenActionException;
import com.saqib.placementportal.exception.ResourceNotFoundException;
import com.saqib.placementportal.repository.CompanyRepository;
import com.saqib.placementportal.util.ApiMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public CompanyService(
            CompanyRepository companyRepository,
            CurrentUserService currentUserService,
            NotificationService notificationService
    ) {
        this.companyRepository = companyRepository;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public Company requireCurrentCompany() {
        UserAccount user = currentUserService.user();
        if (user.primaryRole() != RoleName.COMPANY) {
            throw new ForbiddenActionException("Company role is required");
        }
        return companyRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));
    }

    @Transactional(readOnly = true)
    public Company requireApprovedCurrentCompany() {
        Company company = requireCurrentCompany();
        if (!company.isApproved()) {
            throw new ForbiddenActionException("Company must be approved by admin first");
        }
        if (company.getUser().isBlacklisted()) {
            throw new ForbiddenActionException("Company is blacklisted");
        }
        return company;
    }

    @Transactional(readOnly = true)
    public CompanyResponse myProfile() {
        return ApiMapper.company(requireCurrentCompany());
    }

    @Transactional
    public CompanyResponse updateMyProfile(CompanyProfileRequest request) {
        Company company = requireCurrentCompany();
        company.setCompanyName(request.companyName());
        company.setWebsite(request.website());
        company.setLocation(request.location());
        company.setDescription(request.description());
        return ApiMapper.company(companyRepository.save(company));
    }

    @Transactional(readOnly = true)
    public PageResponse<CompanyResponse> search(String query, Boolean approved, int page, int size) {
        return ApiMapper.page(companyRepository.search(blankToNull(query), approved, PageRequest.of(page, size)), ApiMapper::company);
    }

    @Transactional(readOnly = true)
    public PageResponse<CompanyResponse> pending(int page, int size) {
        return ApiMapper.page(companyRepository.findByApprovedFalse(PageRequest.of(page, size)), ApiMapper::company);
    }

    @Transactional
    @CacheEvict(value = {"drives", "companies", "stats"}, allEntries = true)
    public CompanyResponse approve(Long companyId, boolean approved) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        company.setApproved(approved);
        Company saved = companyRepository.save(company);
        notificationService.send(saved.getUser(), "Company review updated",
                approved ? "Your company profile is approved." : "Your company profile approval was removed.");
        return ApiMapper.company(saved);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
