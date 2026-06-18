package com.saqib.placementportal.service;

import com.saqib.placementportal.dto.ApiDtos.DriveRequest;
import com.saqib.placementportal.dto.ApiDtos.DriveResponse;
import com.saqib.placementportal.dto.ApiDtos.PageResponse;
import com.saqib.placementportal.entity.Company;
import com.saqib.placementportal.entity.DriveStatus;
import com.saqib.placementportal.entity.PlacementDrive;
import com.saqib.placementportal.entity.RoleName;
import com.saqib.placementportal.entity.UserAccount;
import com.saqib.placementportal.exception.ForbiddenActionException;
import com.saqib.placementportal.exception.ResourceNotFoundException;
import com.saqib.placementportal.repository.ApplicationRepository;
import com.saqib.placementportal.repository.PlacementDriveRepository;
import com.saqib.placementportal.util.ApiMapper;
import java.util.List;
import java.util.Set;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriveService {
    private static final Set<String> ALLOWED_SORTS = Set.of("createdAt", "deadline", "title", "annualPackage");

    private final PlacementDriveRepository driveRepository;
    private final ApplicationRepository applicationRepository;
    private final CompanyService companyService;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public DriveService(
            PlacementDriveRepository driveRepository,
            ApplicationRepository applicationRepository,
            CompanyService companyService,
            CurrentUserService currentUserService,
            NotificationService notificationService
    ) {
        this.driveRepository = driveRepository;
        this.applicationRepository = applicationRepository;
        this.companyService = companyService;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
    }

    @Transactional
    @CacheEvict(value = {"drives", "stats"}, allEntries = true)
    public DriveResponse create(DriveRequest request) {
        Company company = companyService.requireApprovedCurrentCompany();
        PlacementDrive drive = new PlacementDrive(company);
        applyRequest(drive, request);
        PlacementDrive saved = driveRepository.save(drive);
        notificationService.notifyAdmins("New placement drive pending",
                company.getCompanyName() + " submitted " + saved.getTitle() + " for approval.");
        return ApiMapper.drive(saved, this::applicantCount);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "drives", key = "'approved:' + #query + ':' + #page + ':' + #size + ':' + #sort")
    public PageResponse<DriveResponse> approved(String query, int page, int size, String sort) {
        return ApiMapper.page(
                driveRepository.search(blankToNull(query), DriveStatus.APPROVED, null, pageable(page, size, sort)),
                drive -> ApiMapper.drive(drive, this::applicantCount)
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<DriveResponse> pending(int page, int size) {
        return ApiMapper.page(
                driveRepository.findByStatus(DriveStatus.PENDING, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))),
                drive -> ApiMapper.drive(drive, this::applicantCount)
        );
    }

    @Transactional(readOnly = true)
    public List<DriveResponse> myCompanyDrives() {
        UserAccount user = currentUserService.user();
        return driveRepository.findByCompanyUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(drive -> ApiMapper.drive(drive, this::applicantCount))
                .toList();
    }

    @Transactional(readOnly = true)
    public DriveResponse get(Long id) {
        PlacementDrive drive = driveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));
        UserAccount user = currentUserService.user();
        boolean admin = user.primaryRole() == RoleName.ADMIN;
        boolean owner = user.primaryRole() == RoleName.COMPANY && drive.getCompany().getUser().getId().equals(user.getId());
        if (drive.getStatus() != DriveStatus.APPROVED && !admin && !owner) {
            throw new ForbiddenActionException("Drive is not approved yet");
        }
        return ApiMapper.drive(drive, this::applicantCount);
    }

    @Transactional
    @CacheEvict(value = {"drives", "stats"}, allEntries = true)
    public DriveResponse updateStatus(Long id, DriveStatus status) {
        PlacementDrive drive = driveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));
        drive.setStatus(status);
        PlacementDrive saved = driveRepository.save(drive);
        notificationService.send(saved.getCompany().getUser(), "Drive status updated",
                saved.getTitle() + " is now " + saved.getStatus().name().toLowerCase() + ".");
        return ApiMapper.drive(saved, this::applicantCount);
    }

    long applicantCount(PlacementDrive drive) {
        return applicationRepository.countByDriveId(drive.getId());
    }

    private void applyRequest(PlacementDrive drive, DriveRequest request) {
        drive.setTitle(request.title());
        drive.setJobRole(request.jobRole());
        drive.setLocation(request.location());
        drive.setDescription(request.description());
        drive.setMinCgpa(request.minCgpa());
        drive.setEligibleBranches(request.eligibleBranches());
        drive.setAnnualPackage(request.annualPackage());
        drive.setDeadline(request.deadline());
    }

    private Pageable pageable(int page, int size, String sort) {
        String property = sort == null || sort.isBlank() ? "createdAt" : sort.trim();
        Sort.Direction direction = Sort.Direction.DESC;
        if (property.startsWith("-")) {
            property = property.substring(1);
        } else if (property.startsWith("+")) {
            property = property.substring(1);
            direction = Sort.Direction.ASC;
        }
        if (!ALLOWED_SORTS.contains(property)) {
            property = "createdAt";
        }
        return PageRequest.of(page, Math.min(Math.max(size, 1), 50), Sort.by(direction, property));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
