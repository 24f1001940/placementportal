package com.saqib.placementportal.service;

import com.saqib.placementportal.dto.ApiDtos.DashboardStats;
import com.saqib.placementportal.dto.ApiDtos.PageResponse;
import com.saqib.placementportal.dto.ApiDtos.UserResponse;
import com.saqib.placementportal.entity.Application;
import com.saqib.placementportal.entity.ApplicationStatus;
import com.saqib.placementportal.entity.DriveStatus;
import com.saqib.placementportal.entity.RoleName;
import com.saqib.placementportal.entity.UserAccount;
import com.saqib.placementportal.exception.ResourceNotFoundException;
import com.saqib.placementportal.repository.ApplicationRepository;
import com.saqib.placementportal.repository.CompanyRepository;
import com.saqib.placementportal.repository.PlacementDriveRepository;
import com.saqib.placementportal.repository.StudentRepository;
import com.saqib.placementportal.repository.UserRepository;
import com.saqib.placementportal.util.ApiMapper;
import java.nio.charset.StandardCharsets;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final PlacementDriveRepository driveRepository;
    private final ApplicationRepository applicationRepository;

    public AdminService(
            UserRepository userRepository,
            StudentRepository studentRepository,
            CompanyRepository companyRepository,
            PlacementDriveRepository driveRepository,
            ApplicationRepository applicationRepository
    ) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.companyRepository = companyRepository;
        this.driveRepository = driveRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable("stats")
    public DashboardStats stats() {
        return new DashboardStats(
                studentRepository.count(),
                companyRepository.count(),
                driveRepository.count(),
                applicationRepository.count(),
                companyRepository.countByApprovedFalse(),
                driveRepository.countByStatus(DriveStatus.PENDING),
                applicationRepository.countByStatus(ApplicationStatus.SHORTLISTED),
                applicationRepository.countByStatus(ApplicationStatus.SELECTED)
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> users(String query, RoleName role, int page, int size) {
        return ApiMapper.page(userRepository.search(blankToNull(query), role, PageRequest.of(page, size)), ApiMapper::user);
    }

    @Transactional
    @CacheEvict(value = {"drives", "companies", "stats"}, allEntries = true)
    public UserResponse blacklist(Long userId, boolean blacklisted) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setBlacklisted(blacklisted);
        return ApiMapper.user(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public byte[] applicationsCsv() {
        StringBuilder csv = new StringBuilder("applicationId,studentName,studentEmail,company,drive,status,appliedAt\n");
        for (Application application : applicationRepository.findAll()) {
            csv.append(application.getId()).append(',')
                    .append(escape(application.getStudent().getUser().getFullName())).append(',')
                    .append(escape(application.getStudent().getUser().getEmail())).append(',')
                    .append(escape(application.getDrive().getCompany().getCompanyName())).append(',')
                    .append(escape(application.getDrive().getTitle())).append(',')
                    .append(application.getStatus()).append(',')
                    .append(application.getAppliedAt()).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
