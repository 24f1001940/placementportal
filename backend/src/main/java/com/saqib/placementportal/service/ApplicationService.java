package com.saqib.placementportal.service;

import com.saqib.placementportal.dto.ApiDtos.ApplicationRequest;
import com.saqib.placementportal.dto.ApiDtos.ApplicationResponse;
import com.saqib.placementportal.entity.Application;
import com.saqib.placementportal.entity.ApplicationStatus;
import com.saqib.placementportal.entity.DriveStatus;
import com.saqib.placementportal.entity.PlacementDrive;
import com.saqib.placementportal.entity.RoleName;
import com.saqib.placementportal.entity.Student;
import com.saqib.placementportal.entity.UserAccount;
import com.saqib.placementportal.exception.ConflictException;
import com.saqib.placementportal.exception.ForbiddenActionException;
import com.saqib.placementportal.exception.ResourceNotFoundException;
import com.saqib.placementportal.repository.ApplicationRepository;
import com.saqib.placementportal.repository.PlacementDriveRepository;
import com.saqib.placementportal.util.ApiMapper;
import java.time.LocalDate;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final PlacementDriveRepository driveRepository;
    private final StudentService studentService;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            PlacementDriveRepository driveRepository,
            StudentService studentService,
            CurrentUserService currentUserService,
            NotificationService notificationService
    ) {
        this.applicationRepository = applicationRepository;
        this.driveRepository = driveRepository;
        this.studentService = studentService;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
    }

    @Transactional
    @CacheEvict(value = {"drives", "stats"}, allEntries = true)
    public ApplicationResponse apply(ApplicationRequest request) {
        Student student = studentService.requireCurrentStudent();
        if (student.getUser().isBlacklisted()) {
            throw new ForbiddenActionException("Blacklisted students cannot apply");
        }
        PlacementDrive drive = driveRepository.findById(request.driveId())
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));
        if (drive.getStatus() != DriveStatus.APPROVED || !drive.isActive()) {
            throw new ForbiddenActionException("Drive is not open for applications");
        }
        if (drive.getDeadline() != null && drive.getDeadline().isBefore(LocalDate.now())) {
            throw new ForbiddenActionException("Drive deadline has passed");
        }
        if (drive.getMinCgpa() != null && student.getCgpa() != null && student.getCgpa().compareTo(drive.getMinCgpa()) < 0) {
            throw new ForbiddenActionException("Student does not meet the minimum CGPA");
        }
        if (applicationRepository.existsByStudentIdAndDriveId(student.getId(), drive.getId())) {
            throw new ConflictException("Student already applied for this drive");
        }
        Application saved = applicationRepository.save(new Application(student, drive, request.coverLetter()));
        notificationService.send(drive.getCompany().getUser(), "New applicant",
                student.getUser().getFullName() + " applied for " + drive.getTitle() + ".");
        return ApiMapper.application(saved, this::applicantCount);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> myApplications() {
        UserAccount user = currentUserService.user();
        return applicationRepository.findByStudentUserIdOrderByAppliedAtDesc(user.getId()).stream()
                .map(application -> ApiMapper.application(application, this::applicantCount))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> companyApplications() {
        UserAccount user = currentUserService.user();
        if (user.primaryRole() != RoleName.COMPANY) {
            throw new ForbiddenActionException("Company role is required");
        }
        return applicationRepository.findByDriveCompanyUserIdOrderByAppliedAtDesc(user.getId()).stream()
                .map(application -> ApiMapper.application(application, this::applicantCount))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> driveApplications(Long driveId) {
        UserAccount user = currentUserService.user();
        PlacementDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));
        boolean admin = user.primaryRole() == RoleName.ADMIN;
        boolean owner = user.primaryRole() == RoleName.COMPANY && drive.getCompany().getUser().getId().equals(user.getId());
        if (!admin && !owner) {
            throw new ForbiddenActionException("Only admin or drive owner can view applicants");
        }
        return applicationRepository.findByDriveIdOrderByAppliedAtDesc(driveId).stream()
                .map(application -> ApiMapper.application(application, this::applicantCount))
                .toList();
    }

    @Transactional
    public ApplicationResponse updateStatus(Long applicationId, ApplicationStatus status) {
        UserAccount user = currentUserService.user();
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        boolean admin = user.primaryRole() == RoleName.ADMIN;
        boolean owner = user.primaryRole() == RoleName.COMPANY
                && application.getDrive().getCompany().getUser().getId().equals(user.getId());
        if (!admin && !owner) {
            throw new ForbiddenActionException("Only admin or drive owner can update applications");
        }
        application.setStatus(status);
        Application saved = applicationRepository.save(application);
        notificationService.send(saved.getStudent().getUser(), "Application status updated",
                saved.getDrive().getTitle() + " is now " + status.name().toLowerCase() + ".");
        return ApiMapper.application(saved, this::applicantCount);
    }

    private long applicantCount(PlacementDrive drive) {
        return applicationRepository.countByDriveId(drive.getId());
    }
}
