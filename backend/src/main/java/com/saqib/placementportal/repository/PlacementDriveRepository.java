package com.saqib.placementportal.repository;

import com.saqib.placementportal.entity.DriveStatus;
import com.saqib.placementportal.entity.PlacementDrive;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlacementDriveRepository extends JpaRepository<PlacementDrive, Long> {
    long countByStatus(DriveStatus status);

    Page<PlacementDrive> findByStatus(DriveStatus status, Pageable pageable);

    List<PlacementDrive> findByCompanyUserIdOrderByCreatedAtDesc(Long userId);

    @Query("""
            select d from PlacementDrive d
            where (:status is null or d.status = :status)
            and (:companyUserId is null or d.company.user.id = :companyUserId)
            and (
                :query is null
                or lower(d.title) like lower(concat('%', :query, '%'))
                or lower(d.jobRole) like lower(concat('%', :query, '%'))
                or lower(d.company.companyName) like lower(concat('%', :query, '%'))
                or lower(d.location) like lower(concat('%', :query, '%'))
            )
            """)
    Page<PlacementDrive> search(
            @Param("query") String query,
            @Param("status") DriveStatus status,
            @Param("companyUserId") Long companyUserId,
            Pageable pageable
    );
}
