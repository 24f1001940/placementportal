package com.saqib.placementportal.dto;

import com.saqib.placementportal.entity.ApplicationStatus;
import com.saqib.placementportal.entity.DriveStatus;
import com.saqib.placementportal.entity.RoleName;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() {
    }

    public record RegisterRequest(
            @NotBlank String fullName,
            @Email @NotBlank String email,
            @Size(min = 6, max = 120) String password,
            @NotNull RoleName role
    ) {
    }

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {
    }

    public record AuthResponse(String token, UserResponse user) implements Serializable {
    }

    public record UserResponse(
            Long id,
            String fullName,
            String email,
            RoleName role,
            boolean approved,
            boolean blacklisted,
            Instant createdAt
    ) implements Serializable {
    }

    public record StudentProfileRequest(
            String phone,
            String college,
            String branch,
            Integer graduationYear,
            @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal cgpa,
            String skills
    ) {
    }

    public record StudentResponse(
            Long id,
            UserResponse user,
            String phone,
            String college,
            String branch,
            Integer graduationYear,
            BigDecimal cgpa,
            String skills,
            ResumeResponse resume
    ) implements Serializable {
    }

    public record CompanyProfileRequest(
            @NotBlank String companyName,
            String website,
            String location,
            String description
    ) {
    }

    public record CompanyResponse(
            Long id,
            UserResponse user,
            String companyName,
            String website,
            String location,
            String description,
            boolean approved
    ) implements Serializable {
    }

    public record DriveRequest(
            @NotBlank String title,
            @NotBlank String jobRole,
            String location,
            String description,
            @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal minCgpa,
            String eligibleBranches,
            @DecimalMin("0.0") BigDecimal annualPackage,
            @FutureOrPresent LocalDate deadline
    ) {
    }

    public record DriveResponse(
            Long id,
            String title,
            String jobRole,
            String location,
            String description,
            BigDecimal minCgpa,
            String eligibleBranches,
            BigDecimal annualPackage,
            LocalDate deadline,
            DriveStatus status,
            boolean active,
            Instant createdAt,
            CompanyResponse company,
            long applicantCount
    ) implements Serializable {
    }

    public record ApplicationRequest(
            @NotNull Long driveId,
            String coverLetter
    ) {
    }

    public record ApplicationResponse(
            Long id,
            StudentResponse student,
            DriveResponse drive,
            ApplicationStatus status,
            String coverLetter,
            Instant appliedAt
    ) implements Serializable {
    }

    public record StatusUpdateRequest(@NotNull String status) {
    }

    public record BlacklistRequest(boolean blacklisted) {
    }

    public record DashboardStats(
            long students,
            long companies,
            long drives,
            long applications,
            long pendingCompanies,
            long pendingDrives,
            long shortlistedApplications,
            long selectedApplications
    ) implements Serializable {
    }

    public record ResumeResponse(
            Long id,
            String originalFileName,
            String contentType,
            long fileSize,
            Instant uploadedAt
    ) implements Serializable {
    }

    public record NotificationResponse(
            Long id,
            String title,
            String message,
            boolean read,
            Instant createdAt
    ) implements Serializable {
    }

    public record NotificationMessage(Long userId, String title, String message) implements Serializable {
    }

    public record PageResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last
    ) implements Serializable {
    }
}
