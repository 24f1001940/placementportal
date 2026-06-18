package com.saqib.placementportal.repository;

import com.saqib.placementportal.entity.ResumeDocument;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<ResumeDocument, Long> {
    Optional<ResumeDocument> findByStudentId(Long studentId);
}
