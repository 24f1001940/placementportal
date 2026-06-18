package com.saqib.placementportal.util;

import com.saqib.placementportal.dto.ApiDtos.ApplicationResponse;
import com.saqib.placementportal.dto.ApiDtos.CompanyResponse;
import com.saqib.placementportal.dto.ApiDtos.DriveResponse;
import com.saqib.placementportal.dto.ApiDtos.NotificationResponse;
import com.saqib.placementportal.dto.ApiDtos.PageResponse;
import com.saqib.placementportal.dto.ApiDtos.ResumeResponse;
import com.saqib.placementportal.dto.ApiDtos.StudentResponse;
import com.saqib.placementportal.dto.ApiDtos.UserResponse;
import com.saqib.placementportal.entity.Application;
import com.saqib.placementportal.entity.Company;
import com.saqib.placementportal.entity.Notification;
import com.saqib.placementportal.entity.PlacementDrive;
import com.saqib.placementportal.entity.ResumeDocument;
import com.saqib.placementportal.entity.Student;
import com.saqib.placementportal.entity.UserAccount;
import java.util.function.ToLongFunction;
import org.springframework.data.domain.Page;

public final class ApiMapper {
    private ApiMapper() {
    }

    public static UserResponse user(UserAccount user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.primaryRole(),
                user.isApproved(),
                user.isBlacklisted(),
                user.getCreatedAt()
        );
    }

    public static ResumeResponse resume(ResumeDocument resume) {
        if (resume == null) {
            return null;
        }
        return new ResumeResponse(
                resume.getId(),
                resume.getOriginalFileName(),
                resume.getContentType(),
                resume.getFileSize(),
                resume.getUploadedAt()
        );
    }

    public static StudentResponse student(Student student) {
        if (student == null) {
            return null;
        }
        return new StudentResponse(
                student.getId(),
                user(student.getUser()),
                student.getPhone(),
                student.getCollege(),
                student.getBranch(),
                student.getGraduationYear(),
                student.getCgpa(),
                student.getSkills(),
                resume(student.getResume())
        );
    }

    public static CompanyResponse company(Company company) {
        if (company == null) {
            return null;
        }
        return new CompanyResponse(
                company.getId(),
                user(company.getUser()),
                company.getCompanyName(),
                company.getWebsite(),
                company.getLocation(),
                company.getDescription(),
                company.isApproved()
        );
    }

    public static DriveResponse drive(PlacementDrive drive, ToLongFunction<PlacementDrive> applicantCounter) {
        return new DriveResponse(
                drive.getId(),
                drive.getTitle(),
                drive.getJobRole(),
                drive.getLocation(),
                drive.getDescription(),
                drive.getMinCgpa(),
                drive.getEligibleBranches(),
                drive.getAnnualPackage(),
                drive.getDeadline(),
                drive.getStatus(),
                drive.isActive(),
                drive.getCreatedAt(),
                company(drive.getCompany()),
                applicantCounter.applyAsLong(drive)
        );
    }

    public static ApplicationResponse application(Application application, ToLongFunction<PlacementDrive> applicantCounter) {
        return new ApplicationResponse(
                application.getId(),
                student(application.getStudent()),
                drive(application.getDrive(), applicantCounter),
                application.getStatus(),
                application.getCoverLetter(),
                application.getAppliedAt()
        );
    }

    public static NotificationResponse notification(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isReadFlag(),
                notification.getCreatedAt()
        );
    }

    public static <T, R> PageResponse<R> page(Page<T> page, java.util.function.Function<T, R> mapper) {
        return new PageResponse<>(
                page.map(mapper).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
