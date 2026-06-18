package com.saqib.placementportal.repository;

import com.saqib.placementportal.entity.Application;
import com.saqib.placementportal.entity.ApplicationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByStudentIdAndDriveId(Long studentId, Long driveId);

    long countByStatus(ApplicationStatus status);

    long countByDriveId(Long driveId);

    List<Application> findByStudentUserIdOrderByAppliedAtDesc(Long userId);

    List<Application> findByDriveCompanyUserIdOrderByAppliedAtDesc(Long userId);

    List<Application> findByDriveIdOrderByAppliedAtDesc(Long driveId);
}
